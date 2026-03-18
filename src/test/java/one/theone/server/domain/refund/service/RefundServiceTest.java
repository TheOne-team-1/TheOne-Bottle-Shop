package one.theone.server.domain.refund.service;

import one.theone.server.common.config.jpa.JpaAuditingConfig;
import one.theone.server.common.config.querydsl.QueryDslConfig;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import one.theone.server.domain.event.dto.EventRewardRequest;
import one.theone.server.domain.event.entity.EventLog;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.repository.EventLogRepository;
import one.theone.server.domain.event.repository.EventRewardRepository;
import one.theone.server.domain.freebie.entity.Freebie;
import one.theone.server.domain.freebie.repository.FreebieRepository;
import one.theone.server.domain.freebie.service.FreebieService;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.entity.PointLog;
import one.theone.server.domain.point.entity.PointUseDetail;
import one.theone.server.domain.point.repository.PointUseDetailRepository;
import one.theone.server.domain.point.service.PointService;
import one.theone.server.domain.product.service.ProductService;
import one.theone.server.domain.product.service.ProductViewService;
import one.theone.server.domain.refund.dto.request.RefundCreateRequest;
import one.theone.server.domain.refund.dto.response.RefundCreateResponse;
import one.theone.server.domain.refund.entity.Refund;
import one.theone.server.domain.refund.repository.RefundRepository;
import one.theone.server.fixture.PaymentAndRefundTestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({QueryDslConfig.class, JpaAuditingConfig.class, RefundService.class, PointService.class, ProductService.class, FreebieService.class})
class RefundServiceTest extends PaymentAndRefundTestFixture {

    @Autowired private RefundService refundService;

    @Autowired private RefundRepository refundRepository;
    @Autowired private CouponRepository couponRepository;
    @Autowired private MemberCouponRepository memberCouponRepository;
    @Autowired private PointUseDetailRepository pointUseDetailRepository;
    @Autowired private FreebieRepository freebieRepository;
    @Autowired private EventRewardRepository eventRewardRepository;
    @Autowired private EventLogRepository eventLogRepository;

    @MockitoBean private ProductViewService productViewService;

    @AfterEach
    void tearDown() {
        pointUseDetailRepository.deleteAll();
        eventLogRepository.deleteAll();
        eventRewardRepository.deleteAll();
        memberCouponRepository.deleteAll();
        couponRepository.deleteAll();
        freebieRepository.deleteAll();
        refundRepository.deleteAll();
    }

    private CompleteOrderPaymentFixture createCompletedOrderAndPayment(Long memberId) {
        return createCompletedOrderAndPayment(memberId, 100000L);
    }

    private CompleteOrderPaymentFixture createCompletedOrderAndPayment(
            Long memberId, Long memberCouponId, Long usedPoint, Long finalAmount) {
        Order order = Order.create(
                memberId, memberCouponId,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                usedPoint, finalAmount + usedPoint, 0L, finalAmount,
                "testAddress1", "testAddress2");
        order.markCompleted();
        orderRepository.save(order);
        orderIdList.add(order.getId());

        orderDetailRepository.save(
                OrderDetail.create(order.getId(), commonProductId, "testWhiskey", 100000L, 1));

        Payment payment = Payment.register(order.getId(), finalAmount);
        payment.updateComplete();
        paymentRepository.save(payment);

        return new CompleteOrderPaymentFixture(order, payment);
    }

    @Test
    @DisplayName("환불 성공 - 주문 취소, 결제 환불, 환불 완료 전환")
    void processRefund_success() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId());

        // when
        RefundCreateResponse response = refundService.processRefund(
                member.getId(), new RefundCreateRequest(fixture.order().getId(), "testReason"));

        // then
        assertThat(response.orderId()).isEqualTo(fixture.order().getId());
        assertThat(response.paymentId()).isEqualTo(fixture.payment().getId());
        assertThat(response.refundId()).isNotNull();
        assertThat(response.refundAt()).isNotNull();

        Order cancelled = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        Payment refunded = paymentRepository.findById(fixture.payment().getId()).orElseThrow();
        assertThat(refunded.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);

        Refund completed = refundRepository.findById(response.refundId()).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(Refund.RefundStatus.COMPLETED);
        assertThat(completed.getRefundAt()).isNotNull();
    }

    @Test
    @DisplayName("환불 성공 - 상품 재고 복구")
    void processRefund_productStockRestored() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId());

        long beforeStock = productRepository.findById(commonProductId).orElseThrow().getQuantity();

        // when
        refundService.processRefund(member.getId(),
                new RefundCreateRequest(fixture.order().getId(), "단순 변심"));

        // then - 주문 상품 재고 증가
        assertThat(productRepository.findById(commonProductId).orElseThrow().getQuantity())
                .isEqualTo(beforeStock + 1);
    }

    @Test
    @DisplayName("환불 성공 - 사용한 할인 쿠폰 복구")
    void processRefund_couponRestored_available() {
        // given
        Member member = createMember();

        Coupon coupon = couponRepository.save(Coupon.register(
                "testCoupon", Coupon.CouponUseType.RATE, 10000L, 10L, 100L,
                LocalDateTime.now().minusDays(1), LocalDate.now().plusYears(1)));

        MemberCoupon mc = MemberCoupon.issuedByAdmin(member.getId(), coupon.getId());
        mc.useCoupon();
        memberCouponRepository.save(mc);

        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), mc.getId(), 0L, 90000L);

        // when
        refundService.processRefund(member.getId(),
                new RefundCreateRequest(fixture.order().getId(), "testReason"));

        // then - endAt이 미래이므로 AVAILABLE 복구
        MemberCoupon restored = memberCouponRepository.findById(mc.getId()).orElseThrow();
        assertThat(restored.getStatus()).isEqualTo(MemberCoupon.MemberCouponStatus.AVAILABLE);
    }

    @Test
    @DisplayName("환불 성공 - 사용한 할인 쿠폰 복구, endAt 때문에 만료된 케이스")
    void processRefund_couponRestored_expired() {
        // given
        Member member = createMember();

        Coupon coupon = couponRepository.save(Coupon.register(
                "testCoupon", Coupon.CouponUseType.RATE, 10000L, 10L, 100L,
                LocalDateTime.now().minusDays(30), LocalDate.now().minusDays(1)));

        MemberCoupon memberCoupon = MemberCoupon.issuedByAdmin(member.getId(), coupon.getId());
        memberCoupon.useCoupon();
        memberCouponRepository.save(memberCoupon);

        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), memberCoupon.getId(), 0L, 90000L);

        // when
        refundService.processRefund(member.getId(),
                new RefundCreateRequest(fixture.order().getId(), "testReason"));

        // then - endAt이 과거이므로 만료처리
        MemberCoupon restored = memberCouponRepository.findById(memberCoupon.getId()).orElseThrow();
        assertThat(restored.getStatus()).isEqualTo(MemberCoupon.MemberCouponStatus.EXPIRED);
    }

    @Test
    @DisplayName("환불 성공 - 사용 포인트 복구")
    void processRefund_pointRestored() {
        // given
        Member member = createMember();
        long usedPoint = 5000L;
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), null, usedPoint, 95000L);

        // 이전 구매에서 적립된 포인트 로그 (보유 포인트 10000)
        PointLog earnLog = pointLogRepository.save(
                PointLog.ofEarn(member.getId(), fixture.order().getId(), fixture.order().getOrderNum(), 10000L, 10000L));

        // 결제 시 포인트 사용, EARN 로그에서 차감
        earnLog.deduct(usedPoint);   // 5000
        pointLogRepository.save(earnLog);

        // 포인트 USE 로그
        pointLogRepository.save(PointLog.ofUse(
                member.getId(), fixture.order().getId(),
                fixture.order().getOrderNum(), -usedPoint, 10000L - usedPoint));

        // 사용 상세 연결
        pointUseDetailRepository.save(
                PointUseDetail.register(earnLog.getId(), fixture.order().getId(), usedPoint));

        // Point 잔액
        Point point = Point.register(member.getId());
        point.updateBalance(10000L - usedPoint);
        pointRepository.save(point);

        // when
        refundService.processRefund(member.getId(), new RefundCreateRequest(fixture.order().getId(), "testReason"));

        // then - 포인트 잔액 5000 복구 (5000 + 5000 = 10000)
        Point restored = pointRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(restored.getBalance()).isEqualTo(10000L);

        // REFUND 타입 로그 생성 확인
        assertThat(pointLogRepository.findAll().stream().anyMatch(pointLog -> pointLog.getType() == PointLog.PointType.REFUND)).isTrue();

        // 복구 확인
        PointLog restoredEarnLog = pointLogRepository.findById(earnLog.getId()).orElseThrow();
        assertThat(restoredEarnLog.getRemainingAmount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("환불 성공 - 이벤트 사은품 재고 복구 및 EventLog 생성")
    void processRefund_freebieRestored_andEventLogFail() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId());

        Freebie freebie = freebieRepository.save(Freebie.register(1L, "testFreebie", 5L));

        EventReward eventReward = eventRewardRepository.save(
                EventReward.registerByRewardType(100L, new EventRewardRequest(EventReward.EventRewardType.FREEBIE, freebie.getId())));

        eventLogRepository.save(EventLog.registerComplete(
                100L, eventReward.getId(), member.getId(), fixture.order().getId()));

        // when
        refundService.processRefund(member.getId(), new RefundCreateRequest(fixture.order().getId(), "testReason"));

        // then - 사은품 재고 증가
        Freebie restoredFreebie = freebieRepository.findById(freebie.getId()).orElseThrow();
        assertThat(restoredFreebie.getQuantity()).isEqualTo(6L);

        // EventLog FAIL 생성 확인
        List<EventLog> failLogs = eventLogRepository.findByOrderIdAndStatus(fixture.order().getId(), EventLog.EventLogStatus.FAIL);
        assertThat(failLogs).hasSize(1);
        assertThat(failLogs.getFirst().getEventRewardId()).isEqualTo(eventReward.getId());
    }

    @Test
    @DisplayName("환불 성공 - 이벤트 쿠폰 회수 및 EventLog FAIL 생성")
    void processRefund_eventCouponRecalled() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId());

        long eventId = 200L;
        Coupon eventCoupon = couponRepository.save(Coupon.register(
                "testCoupon", Coupon.CouponUseType.AMOUNT, 0L, 5000L, 100L,
                LocalDateTime.now().minusDays(1), LocalDate.now().plusYears(1)));

        // 이벤트로 발급된 쿠폰
        MemberCoupon eventMemberCoupon = MemberCoupon.issuedByEvent(member.getId(), eventCoupon.getId(), eventId);
        memberCouponRepository.save(eventMemberCoupon);

        EventReward eventReward = eventRewardRepository.save(
                EventReward.registerByRewardType(eventId, new EventRewardRequest(EventReward.EventRewardType.COUPON, eventCoupon.getId()))
        );

        eventLogRepository.save(EventLog.registerComplete(eventId, eventReward.getId(), member.getId(), fixture.order().getId()));

        // when
        refundService.processRefund(member.getId(),
                new RefundCreateRequest(fixture.order().getId(), "testReason"));

        // then - 이벤트 쿠폰 RECALL
        MemberCoupon recalled = memberCouponRepository.findById(eventMemberCoupon.getId()).orElseThrow();
        assertThat(recalled.getStatus()).isEqualTo(MemberCoupon.MemberCouponStatus.RECALL);

        // EventLog 생성 확인
        List<EventLog> failLogs = eventLogRepository.findByOrderIdAndStatus(
                fixture.order().getId(), EventLog.EventLogStatus.FAIL);
        assertThat(failLogs).hasSize(1);
        assertThat(failLogs.getFirst().getEventRewardId()).isEqualTo(eventReward.getId());
    }

    @Test
    @DisplayName("환불 실패 - 존재하지 않는 주문")
    void processRefund_orderNotFound() {
        Member member = createMember();

        assertThatThrownBy(() -> refundService.processRefund(member.getId(), new RefundCreateRequest(999999L, "testReason")))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("환불 실패 - 타인 주문 접근")
    void processRefund_notMyOrder() {
        // given
        Member owner = createMember();
        Member other  = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(owner.getId());

        // when & then
        assertThatThrownBy(() -> refundService.processRefund(other.getId(), new RefundCreateRequest(fixture.order().getId(), "testReason")))
                .isInstanceOf(ServiceErrorException.class);

        Order unchanged = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("환불 실패 - 확정 주문은 환불 불가")
    void processRefund_orderConfirmed() {
        // given
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");
        order.markCompleted();
        order.markConfirmed();
        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment payment = Payment.register(order.getId(), 100000L);
        payment.updateComplete();
        paymentRepository.save(payment);

        // when & then
        assertThatThrownBy(() -> refundService.processRefund(member.getId(), new RefundCreateRequest(order.getId(), "testReason")))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("환불 실패 - PENDING_PAYMENT 주문은 환불 불가")
    void processRefund_orderPending() {
        // given
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");
        // markCompleted() 없이 PENDING_PAYMENT 상태 유지
        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment payment = Payment.register(order.getId(), 100000L);
        payment.updateComplete();
        paymentRepository.save(payment);

        // when & then
        assertThatThrownBy(() -> refundService.processRefund(member.getId(), new RefundCreateRequest(order.getId(), "testReason")))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("환불 실패 - 이미 완료된 환불 건 재요청")
    void processRefund_alreadyRefunded() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId());

        // 이미 완료 상태인 환불 케이스 생성
        Refund refund = Refund.register(
                fixture.order().getId(), fixture.payment().getId(),
                100000L, Refund.RefundReason.MEMBER_REQUEST, "testReason");
        refund.updateComplete();
        refundRepository.save(refund);

        // when & then
        assertThatThrownBy(() -> refundService.processRefund(member.getId(), new RefundCreateRequest(fixture.order().getId(), "testReason2")))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("환불 실패 - 이벤트로 받은 쿠폰이 이미 사용된 경우 환불 불가")
    void processRefund_eventCouponAlreadyUsed() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId());

        long eventId = 999L;
        Coupon eventCoupon = couponRepository.save(Coupon.register(
                "testCoupon", Coupon.CouponUseType.AMOUNT, 0L, 5000L, 100L,
                LocalDateTime.now().minusDays(1), LocalDate.now().plusYears(1)));

        // 이벤트 쿠폰이 이미 사용된 상태
        MemberCoupon usedEventCoupon = MemberCoupon.issuedByEvent(member.getId(), eventCoupon.getId(), eventId);
        usedEventCoupon.useCoupon();
        memberCouponRepository.save(usedEventCoupon);

        EventReward eventReward = eventRewardRepository.save(EventReward.registerByRewardType(eventId,
                new EventRewardRequest(EventReward.EventRewardType.COUPON, eventCoupon.getId())));

        eventLogRepository.save(EventLog.registerComplete(eventId, eventReward.getId(), member.getId(), fixture.order().getId()));

        // when & then
        assertThatThrownBy(() -> refundService.processRefund(member.getId(), new RefundCreateRequest(fixture.order().getId(), "testReason")))
                .isInstanceOf(ServiceErrorException.class);

        // 사전 검증 실패, 주문 상태 변경 없음
        Order unchanged = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}
