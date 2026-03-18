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
import one.theone.server.domain.freebie.repository.FreebieRepository;
import one.theone.server.domain.freebie.service.FreebieService;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.point.repository.PointUseDetailRepository;
import one.theone.server.domain.point.service.PointService;
import one.theone.server.domain.product.service.ProductService;
import one.theone.server.domain.product.service.ProductViewService;
import one.theone.server.domain.refund.dto.request.AdminRefundCreateRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({QueryDslConfig.class, JpaAuditingConfig.class, RefundService.class, PointService.class, ProductService.class, FreebieService.class})
class AdminRefundServiceTest extends PaymentAndRefundTestFixture {

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

    @Test
    @DisplayName("관리자 환불 성공 - ADMIN_REQUEST, 상태 전환 확인")
    void processAdminRefund_success_adminRequest() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), 100000L);

        // when
        RefundCreateResponse response = refundService.processAdminRefund(
                new AdminRefundCreateRequest(fixture.order().getId(), Refund.RefundReason.ADMIN_REQUEST, "ADMIN_REQUEST"));

        // then
        assertThat(response.orderId()).isEqualTo(fixture.order().getId());
        assertThat(response.refundId()).isNotNull();
        assertThat(response.refundAt()).isNotNull();

        Order cancelled = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        Payment refunded = paymentRepository.findById(fixture.payment().getId()).orElseThrow();
        assertThat(refunded.getStatus()).isEqualTo(Payment.PaymentStatus.REFUNDED);

        Refund completed = refundRepository.findById(response.refundId()).orElseThrow();
        assertThat(completed.getStatus()).isEqualTo(Refund.RefundStatus.COMPLETED);
        assertThat(completed.getReason()).isEqualTo(Refund.RefundReason.ADMIN_REQUEST);
    }

    @Test
    @DisplayName("관리자 환불 성공 - OUT_OF_STOCK")
    void processAdminRefund_success_outOfStock() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), 100000L);

        // when
        refundService.processAdminRefund(
                new AdminRefundCreateRequest(fixture.order().getId(), Refund.RefundReason.OUT_OF_STOCK, "OUT_OF_STOCK"));

        // then
        Refund completed = refundRepository.findAll().stream()
                .filter(r -> r.getOrderId().equals(fixture.order().getId()))
                .findFirst().orElseThrow();
        assertThat(completed.getReason()).isEqualTo(Refund.RefundReason.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("관리자 환불 성공 - 타 회원 주문 환불")
    void processAdminRefund_canRefundAnyMemberOrder() {
        // given
        Member owner = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(owner.getId(), 100000L);

        // when - owner의 주문을 memberId 없이 관리자가 직접 처리
        RefundCreateResponse response = refundService.processAdminRefund(
                new AdminRefundCreateRequest(fixture.order().getId(), Refund.RefundReason.ADMIN_REQUEST, "ADMIN_REQUEST"));

        // then
        assertThat(response.orderId()).isEqualTo(fixture.order().getId());
        Order cancelled = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("관리자 환불 성공 - 이벤트 쿠폰이 이미 USED도 환불 진행, 쿠폰 상태 변경 없음")
    void processAdminRefund_eventCouponUsed_skipped() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), 100000L);

        long eventId = 100L;
        Coupon eventCoupon = couponRepository.save(Coupon.register(
                "testCoupon", Coupon.CouponUseType.AMOUNT, 0L, 5000L, 100L,
                LocalDateTime.now().minusDays(1), LocalDate.now().plusYears(1)));

        // 이벤트 쿠폰을 이미 다른 주문에서 사용한 상태
        MemberCoupon usedEventCoupon = MemberCoupon.issuedByEvent(member.getId(), eventCoupon.getId(), eventId);
        usedEventCoupon.useCoupon();
        memberCouponRepository.save(usedEventCoupon);

        EventReward eventReward = eventRewardRepository.save(
                EventReward.registerByRewardType(eventId,
                        new EventRewardRequest(EventReward.EventRewardType.COUPON, eventCoupon.getId())));

        eventLogRepository.save(EventLog.registerComplete(
                eventId, eventReward.getId(), member.getId(), fixture.order().getId()));

        // when - 예외 없이 환불 성공
        RefundCreateResponse response = refundService.processAdminRefund(
                new AdminRefundCreateRequest(fixture.order().getId(), Refund.RefundReason.ADMIN_REQUEST, "ADMIN_REQUEST"));

        // then - 환불 완료
        assertThat(response.refundAt()).isNotNull();
        Order cancelled = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        // 쿠폰 상태는 USED 그대로 유지
        MemberCoupon unchanged = memberCouponRepository.findById(usedEventCoupon.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(MemberCoupon.MemberCouponStatus.USED);
    }

    @Test
    @DisplayName("관리자 환불 성공 - 이벤트 쿠폰이 AVAILABLE면 정상 회수")
    void processAdminRefund_eventCouponAvailable_recalled() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), 100000L);

        long eventId = 200L;
        Coupon eventCoupon = couponRepository.save(Coupon.register(
                "testCoupon", Coupon.CouponUseType.AMOUNT, 0L, 5000L, 100L,
                LocalDateTime.now().minusDays(1), LocalDate.now().plusYears(1)));

        // AVAILABLE 상태의 이벤트 쿠폰
        MemberCoupon availableCoupon = MemberCoupon.issuedByEvent(member.getId(), eventCoupon.getId(), eventId);
        memberCouponRepository.save(availableCoupon);

        EventReward eventReward = eventRewardRepository.save(
                EventReward.registerByRewardType(eventId,
                        new EventRewardRequest(EventReward.EventRewardType.COUPON, eventCoupon.getId())));

        eventLogRepository.save(EventLog.registerComplete(
                eventId, eventReward.getId(), member.getId(), fixture.order().getId()));

        // when
        refundService.processAdminRefund(
                new AdminRefundCreateRequest(fixture.order().getId(), Refund.RefundReason.ADMIN_REQUEST, "ADMIN_REQUEST"));

        // then
        MemberCoupon recalled = memberCouponRepository.findById(availableCoupon.getId()).orElseThrow();
        assertThat(recalled.getStatus()).isEqualTo(MemberCoupon.MemberCouponStatus.RECALL);
    }

    @Test
    @DisplayName("관리자 환불 실패 - 존재하지 않는 주문")
    void processAdminRefund_orderNotFound() {
        assertThatThrownBy(() ->
                refundService.processAdminRefund(
                        new AdminRefundCreateRequest(999999L, Refund.RefundReason.ADMIN_REQUEST, "ADMIN_REQUEST"))
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("관리자 환불 실패 - CONFIRMED 주문은 환불 불가 - 회원, 관리자 동일")
    void processAdminRefund_orderConfirmed() {
        // given
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");
        order.markCompleted();
        order.markConfirmed();
        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment payment = Payment.register(order.getId(), 100000L);
        payment.updateComplete();
        paymentRepository.save(payment);

        // when & then
        assertThatThrownBy(() ->
                refundService.processAdminRefund(
                        new AdminRefundCreateRequest(order.getId(), Refund.RefundReason.ADMIN_REQUEST, "ADMIN_REQUEST"))
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("관리자 환불 실패 - 이미 완료된 환불 건 재처리")
    void processAdminRefund_alreadyRefunded() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), 100000L);

        Refund existingRefund = Refund.register(
                fixture.order().getId(), fixture.payment().getId(),
                100000L, Refund.RefundReason.MEMBER_REQUEST, "testReason");
        existingRefund.updateComplete();
        refundRepository.save(existingRefund);

        // when & then
        assertThatThrownBy(() ->
                refundService.processAdminRefund(
                        new AdminRefundCreateRequest(fixture.order().getId(), Refund.RefundReason.ADMIN_REQUEST, "testReason2"))
        ).isInstanceOf(ServiceErrorException.class);
    }
}
