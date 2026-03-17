package one.theone.server.domain.payment.service;

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
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.payment.dto.response.PaymentCreateResponse;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.payment.repository.PaymentRepository;
import one.theone.server.domain.point.service.PointService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static one.theone.server.common.exception.domain.CouponExceptionEnum.*;

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
}
