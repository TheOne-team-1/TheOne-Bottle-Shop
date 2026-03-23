package one.theone.server.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.coupon.dto.response.CouponIssueResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import one.theone.server.domain.coupon.service.CouponIssueService;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.service.EventService;
import one.theone.server.domain.freebie.service.FreebieService;
import one.theone.server.domain.freebie.service.FreebieStockService;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.dto.request.PaymentCreateRequest;
import one.theone.server.domain.payment.dto.response.PaymentCreateResponse;
import one.theone.server.domain.product.service.ProductService;
import one.theone.server.domain.product.service.ProductStockService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static one.theone.server.common.exception.domain.CouponExceptionEnum.*;
import static one.theone.server.common.exception.domain.OrderExceptionEnum.ERR_ORDER_NOT_FOUND;
import static one.theone.server.common.exception.domain.PaymentExceptionEnum.ERR_INVALID_PENDING;
import static one.theone.server.common.exception.domain.PaymentExceptionEnum.ERR_NOT_MY_ORDER;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFacade {
    private final PaymentService paymentService;
    //private final ProductStockService productStockService;
    private final ProductService productService;
    //private final FreebieStockService freebieStockService;
    private final FreebieService freebieService;
    private final CouponIssueService couponIssueService;
    private final EventService eventService;

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRepository couponRepository;

    // 상품 재고, 사은품 재고 선점 및 검증부
    public PaymentCreateResponse createPayment(Long memberId, PaymentCreateRequest request) {
        // 주문 정보 확인
        Order order = orderRepository.findById(request.orderId()).orElseThrow(() -> new ServiceErrorException(ERR_ORDER_NOT_FOUND));

        // 주문 검증 - 사용자 및 상태 검증
        if (!order.getMemberId().equals(memberId)) {
            throw new ServiceErrorException(ERR_NOT_MY_ORDER);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ServiceErrorException(ERR_INVALID_PENDING);
        }

        // 주문한 상품 받아오기
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());

        if (order.getMemberCouponId() != null) {
            MemberCoupon memberCoupon = memberCouponRepository.findById(order.getMemberCouponId()).orElseThrow(() -> new ServiceErrorException(ERR_MEMBER_COUPON_NOT_FOUND));

            if (!memberCoupon.isAvailable()) throw new ServiceErrorException(ERR_MEMBER_COUPON_NOT_AVAILABLE);

            Coupon coupon = couponRepository.findById(memberCoupon.getCouponId()).orElseThrow(() -> new ServiceErrorException(ERR_COUPON_NOT_FOUND));

            // 주문 가격이 쿠폰의 최소가격 미만이면 오류 발생
            if (order.getTotalAmount() < coupon.getMinPrice()) throw new ServiceErrorException(ERR_COUPON_MIN_PRICE);
        }

        // 선점 (주문 상품 재고 선점, 사은품 제공시 사은품 재고 선점)
        // 주문 상품 재고 선점
        Map<Long, Long> preemptProductMap = new HashMap<>();
        try {
            for (OrderDetail detail : orderDetails) {
                //productStockService.decreaseStock(detail.getProductId(), detail.getQuantity().longValue());
                productService.decreaseStockWithRedisson(detail.getProductId(), detail.getQuantity().longValue());
                preemptProductMap.put(detail.getProductId(), detail.getQuantity().longValue());
            }
        } catch (Exception e) {
            //preemptProductMap.forEach((productId, quantity) -> productStockService.increaseStock(productId, quantity));
            preemptProductMap.forEach((productId, quantity) -> productService.increaseStockWithRedisson(productId, quantity));
            throw e;
        }

        // 쿠폰 또는 사은품 제공여부 체크
        // 쿠폰 발급 선점
        // 사은품 재고 선점 - 사은품은 주문당 1개만 지급
        List<EventReward> eventRewardList = eventService.getEventRewardIfExists(order, orderDetails);
        List<CouponIssueResponse> preemptMemberCouponList = new ArrayList<>();
        List<Long> preemptFreebieList = new ArrayList<>();
        try {
            for (EventReward eventReward : eventRewardList) {
                if (eventReward.getRewardType() == EventReward.EventRewardType.FREEBIE) {
                    // 사은품은 주문당 1개 지급으로 고정
                    //freebieStockService.decreaseStockWithLock(eventReward.getFreebieId(), 1L);
                    freebieService.decreaseStockWithRedisson(eventReward.getFreebieId(), 1L);
                    preemptFreebieList.add(eventReward.getFreebieId());
                    eventService.pauseEventIfFreebieSoldOut(eventReward.getFreebieId()); // 사은품 재고가 떨어지면 Event 상태 Pause 변경
                }

                if(eventReward.getRewardType() == EventReward.EventRewardType.COUPON) {
                    CouponIssueResponse response = couponIssueService.issueCouponWithLock(eventReward.getCouponId(), order.getMemberId(), eventReward.getEventId());
                    preemptMemberCouponList.add(response);
                    eventService.pauseEventIfCouponSoldOut(eventReward.getCouponId()); // 쿠폰 생성 초과될 경우 Event 상태 Pause 변경
                }
            }
        } catch(Exception e){
            //preemptProductMap.forEach((productId, quantity) -> productStockService.increaseStock(productId, quantity));
            preemptProductMap.forEach((productId, quantity) -> productService.increaseStockWithRedisson(productId, quantity));
            //preemptFreebieList.forEach((freebieId) -> freebieStockService.increaseStockWithLock(freebieId, 1L));
            preemptFreebieList.forEach((freebieId) -> freebieService.increaseStockWithRedisson(freebieId, 1L));
            preemptMemberCouponList.forEach(response -> couponIssueService.cancelIssuanceWithLock(response.couponId(), response.memberCouponId()));
            throw e;
        }

        // 결제 처리
        try {
            // 결제 생성과 포인트 차감 작업 부
            return paymentService.processPayment(order, eventRewardList);
        } catch (Exception e) {
            //reemptProductMap.forEach((productId, quantity) -> productStockService.increaseStock(productId, quantity));
            preemptProductMap.forEach((productId, quantity) -> productService.increaseStockWithRedisson(productId, quantity));
            //preemptFreebieList.forEach(freebieId -> freebieStockService.increaseStockWithLock(freebieId, 1L));
            preemptFreebieList.forEach((freebieId) -> freebieService.increaseStockWithRedisson(freebieId, 1L));
            preemptMemberCouponList.forEach(response -> couponIssueService.cancelIssuanceWithLock(response.couponId(), response.memberCouponId()));
            throw e;
        }
    }
}
