package one.theone.server.domain.refund.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import one.theone.server.domain.event.entity.EventLog;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.repository.EventLogRepository;
import one.theone.server.domain.event.repository.EventRewardRepository;
import one.theone.server.domain.freebie.service.FreebieService;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.payment.repository.PaymentRepository;
import one.theone.server.domain.point.service.PointService;
import one.theone.server.domain.product.service.ProductService;
import one.theone.server.domain.refund.dto.request.RefundCreateRequest;
import one.theone.server.domain.refund.dto.response.RefundCreateResponse;
import one.theone.server.domain.refund.entity.Refund;
import one.theone.server.domain.refund.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static one.theone.server.common.exception.domain.CouponExceptionEnum.ERR_COUPON_NOT_FOUND;
import static one.theone.server.common.exception.domain.CouponExceptionEnum.ERR_MEMBER_COUPON_NOT_FOUND;
import static one.theone.server.common.exception.domain.OrderExceptionEnum.ERR_ORDER_NOT_FOUND;
import static one.theone.server.common.exception.domain.PaymentExceptionEnum.ERR_NOT_MY_ORDER;
import static one.theone.server.common.exception.domain.PaymentExceptionEnum.ERR_PAYMENT_NOT_FOUND;
import static one.theone.server.common.exception.domain.RefundExceptionEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundService {

    private final PointService pointService;
    private final ProductService productService;
    private final FreebieService freebieService;

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRepository couponRepository;
    private final EventLogRepository eventLogRepository;
    private final EventRewardRepository eventRewardRepository;

    public RefundCreateResponse processRefund(Long memberId, RefundCreateRequest request) {
        // 주문 검증
        Order order = orderRepository.findById(request.orderId()).orElseThrow(() -> new ServiceErrorException(ERR_ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new ServiceErrorException(ERR_NOT_MY_ORDER);
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new ServiceErrorException(ERR_ORDER_NOT_REFUNDABLE);
        }

        if (refundRepository.existsByOrderIdAndStatusAndDeletedFalse(order.getId(), Refund.RefundStatus.COMPLETED)) {
            throw new ServiceErrorException(ERR_REFUND_ALREADY_COMPLETED);
        }

        // 이벤트 보상 목록 조회
        List<EventLog> completedEventLogs = eventLogRepository.findByOrderIdAndStatus(order.getId(), EventLog.EventLogStatus.COMPLETE);

        // 이벤트 지급 쿠폰 사용 여부 사전 검증
        for (EventLog eventLog : completedEventLogs) {
            eventRewardRepository.findById(eventLog.getEventRewardId())
                .filter(eventReward -> eventReward.getRewardType() == EventReward.EventRewardType.COUPON)
                .ifPresent(eventReward -> memberCouponRepository.findByMemberIdAndCouponIdAndEventIdAndDeletedFalse(order.getMemberId(), eventReward.getCouponId(),eventReward.getEventId())
                        .ifPresent(memberCoupon -> {
                            if (!memberCoupon.isAvailable()) {
                                throw new ServiceErrorException(ERR_USE_EVENT_COUPON_NOT_REFUND);
                            }
                        })
                );
        }

        // 결제 조회
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElseThrow(() -> new ServiceErrorException(ERR_PAYMENT_NOT_FOUND));

        // 환불 생성
        Refund refund = Refund.register(order.getId(), payment.getId(), order.getFinalAmount(), Refund.RefundReason.MEMBER_REQUEST, request.reasonDescription());
        refundRepository.save(refund);

        // 사용한 쿠폰 복구
        if (order.getMemberCouponId() != null) {
            MemberCoupon memberCoupon = memberCouponRepository.findById(order.getMemberCouponId())
                    .orElseThrow(() -> new ServiceErrorException(ERR_MEMBER_COUPON_NOT_FOUND));
            Coupon coupon = couponRepository.findById(memberCoupon.getCouponId())
                    .orElseThrow(() -> new ServiceErrorException(ERR_COUPON_NOT_FOUND));
            memberCoupon.refundCoupon(coupon.getEndAt());
        }

        // 포인트 복구
        if (order.getUsedPoint() > 0) {
            pointService.refundPoint(memberId, order.getId());
        }

        // 상품 재고 복구
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail detail : orderDetails) {
            productService.increaseStock(detail.getProductId(), detail.getQuantity().longValue());
        }

        // 이벤트 보상 복구 및 취소 로그
        for (EventLog eventLog : completedEventLogs) {
            EventReward eventReward = eventRewardRepository.findById(eventLog.getEventRewardId()).orElse(null);
            if (eventReward == null) continue;

            if (eventReward.getRewardType() == EventReward.EventRewardType.FREEBIE) {
                freebieService.increaseStock(eventReward.getFreebieId(), 1L);
            }

            if (eventReward.getRewardType() == EventReward.EventRewardType.COUPON) {
                memberCouponRepository.findByMemberIdAndCouponIdAndEventIdAndDeletedFalse(
                                order.getMemberId(), eventReward.getCouponId(), eventReward.getEventId())
                        .ifPresent(MemberCoupon::recallByAdmin);
            }

            eventLogRepository.save(EventLog.registerFail(eventLog.getEventId(), eventLog.getEventRewardId(), eventLog.getMemberId(), eventLog.getOrderId()));
        }

        // 주문 취소
        order.markCancelled();

        // 결제 환불 처리
        payment.updateRefund();

        // 환불 완료
        refund.updateComplete();

        return new RefundCreateResponse(
                refund.getId()
                , order.getId()
                , payment.getId()
                , refund.getRefundAt()
        );
    }
}
