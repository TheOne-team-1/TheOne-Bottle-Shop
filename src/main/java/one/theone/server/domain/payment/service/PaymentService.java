package one.theone.server.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.OrderExceptionEnum;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import one.theone.server.domain.event.entity.EventLog;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.repository.EventLogRepository;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.dto.response.PaymentConfirmResponse;
import one.theone.server.domain.payment.dto.response.PaymentCreateResponse;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.payment.repository.PaymentRepository;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.point.service.PointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static one.theone.server.common.exception.domain.CouponExceptionEnum.*;
import static one.theone.server.common.exception.domain.MemberExceptionEnum.ERR_MEMBER_NOT_FOUND;
import static one.theone.server.common.exception.domain.OrderExceptionEnum.ERR_ORDER_NOT_FOUND;
import static one.theone.server.common.exception.domain.PaymentExceptionEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PointService pointService;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponRepository couponRepository;
    private final EventLogRepository eventLogRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PaymentCreateResponse processPayment(Order order, List<EventReward> eventRewardList) {
        // 주문에 쿠폰이 적용 되었는지
        if(order.getMemberCouponId() != null) {
            // 쿠폰 검증
            MemberCoupon memberCoupon = memberCouponRepository.findById(order.getMemberCouponId()).orElseThrow(() -> new ServiceErrorException(ERR_MEMBER_COUPON_NOT_FOUND));

            if(!memberCoupon.isAvailable()) {
                throw new ServiceErrorException(ERR_MEMBER_COUPON_NOT_AVAILABLE);
            }

            Coupon coupon = couponRepository.findById(memberCoupon.getCouponId()).orElseThrow(() -> new ServiceErrorException(ERR_COUPON_NOT_FOUND));

            // 쿠폰 할인가 등록
            Long discountAmount = coupon.calculateDiscount(order.getTotalAmount());
            order.calculateFinalAmount(discountAmount);
            memberCoupon.useCoupon();
        }

        // 포인트 처리
        if(order.getUsedPoint() > 0) {
            pointService.usePoint(order.getMemberId(), order.getId());
        }

        // 결제 생성 -> 결제 완료 처리
        Payment payment = Payment.register(order.getId(), order.getFinalAmount());
        payment.updateComplete();
        paymentRepository.save(payment);

        // 주문 완료
        order.markCompleted();
        orderRepository.save(order);

        // 이벤트 로그 기록
        for (EventReward reward : eventRewardList) {
            eventLogRepository.save(EventLog.registerComplete(reward.getEventId(), reward.getId(), order.getMemberId(), order.getId()));
        }

        return new PaymentCreateResponse(payment.getPaymentUniqueId());
    }

    @Transactional
    public PaymentConfirmResponse processConfirm(Long memberId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ServiceErrorException(ERR_PAYMENT_NOT_FOUND));
        Order order = orderRepository.findById(payment.getOrderId()).orElseThrow(() -> new ServiceErrorException(ERR_ORDER_NOT_FOUND));

        // 주문 검증
        if(!order.getMemberId().equals(memberId)) {
            throw new ServiceErrorException(ERR_NOT_MY_ORDER);
        }

        if(payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new ServiceErrorException(ERR_INVALID_COMPLETE);
        }

        if(order.getStatus() != OrderStatus.COMPLETED) {
            throw new ServiceErrorException(ERR_INVALID_ORDER_COMPLETE);
        }

        // 주문 확정 처리
        order.markConfirmed();

        // 포인트 적립
        pointService.earnPoint(memberId, order.getId(), order.getFinalAmount());

        // 구매 총액 증가 + 등급 갱신
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new ServiceErrorException(ERR_MEMBER_NOT_FOUND));
        member.addPayAmount(order.getFinalAmount());

        return new PaymentConfirmResponse(order.getId(), payment.getId(), payment.getPaymentUniqueId());
    }
}
