package one.theone.server.domain.payment.service;

import one.theone.server.common.config.jpa.JpaAuditingConfig;
import one.theone.server.common.config.querydsl.QueryDslConfig;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.entity.MemberGrade;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.payment.dto.response.PaymentConfirmResponse;
import one.theone.server.domain.payment.entity.Payment;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.service.PointService;
import one.theone.server.fixture.PaymentAndRefundTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({QueryDslConfig.class, JpaAuditingConfig.class, PaymentService.class, PointService.class})
class PaymentServiceConfirmTest extends PaymentAndRefundTestFixture {

    @Autowired private PaymentService paymentService;

    @Test
    @DisplayName("구매 확정 성공 - 주문 확정 전환")
    void processConfirm_success() {
        // given
        Member member = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), 100000L);

        // when
        PaymentConfirmResponse response = paymentService.processConfirm(
                fixture.payment().getId(), member.getId());

        // then
        assertThat(response.orderId()).isEqualTo(fixture.order().getId());
        assertThat(response.paymentId()).isEqualTo(fixture.payment().getId());
        assertThat(response.paymentUniqueId()).startsWith("PAY-");

        Order confirmed = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("구매 확정 성공 - 브론즈 회원 포인트 적립 및 구매 총액 증가")
    void processConfirm_earnPoint() {
        // given
        Member member = createMember();
        long finalAmount = 100000L;
        long expectedEarn = (long) Math.floor((double) finalAmount / 100);
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), finalAmount);

        // when
        paymentService.processConfirm(fixture.payment().getId(), member.getId());

        // then - 포인트 잔액 확인
        Point point = pointRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(point.getBalance()).isEqualTo(expectedEarn);

        // 구매 총액 증가 확인
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getTotalPayAmount()).isEqualTo(finalAmount);
        assertThat(updated.getGrade()).isEqualTo(MemberGrade.BRONZE);
    }

    @Test
    @DisplayName("구매 확정 성공 - 구매 총액이 등급 기준 초과 시 등급 자동 업그레이드")
    void processConfirm_gradeUpgrade() {
        // given
        Member member = createMember();
        member.addPayAmount(1990000L);
        memberRepository.save(member);

        long finalAmount = 50000L; // 확정 후 실버
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(member.getId(), finalAmount);

        // earnPoint는 아직 브론즈 기준 적립
        long expectedEarn = (long) Math.floor((double) finalAmount / 100);

        // when
        paymentService.processConfirm(fixture.payment().getId(), member.getId());

        // then - 포인트는 브론즈 기준으로 적립
        Point point = pointRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(point.getBalance()).isEqualTo(expectedEarn);

        // 등급은 SILVER로 업그레이드
        Member updated = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updated.getGrade()).isEqualTo(MemberGrade.SILVER);
        assertThat(updated.getTotalPayAmount()).isEqualTo(1990000L + finalAmount);
    }

    @Test
    @DisplayName("구매 확정 실패 - 존재하지 않는 paymentId")
    void processConfirm_paymentNotFound() {
        Member member = createMember();
        Long nonExistPaymentId = 999999L;

        assertThatThrownBy(() -> paymentService.processConfirm(nonExistPaymentId, member.getId())).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 다른 회원의 주문 접근")
    void processConfirm_notMyOrder() {
        // given
        Member owner = createMember();
        Member other = createMember();
        CompleteOrderPaymentFixture fixture = createCompletedOrderAndPayment(owner.getId(), 100000L);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(fixture.payment().getId(), other.getId())
        ).isInstanceOf(ServiceErrorException.class);

        // 주문 상태 변화 없음
        Order unchanged = orderRepository.findById(fixture.order().getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("구매 확정 실패 - 결제가 완료 상태가 아닌 경우")
    void processConfirm_paymentNotCompleted() {
        // given - PENDING 상태 Payment 직접 생성
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");
        order.markCompleted();
        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment pendingPayment = Payment.register(order.getId(), 100000L); // PENDING 상태 유지
        paymentRepository.save(pendingPayment);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(pendingPayment.getId(), member.getId())
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 주문이 완료 상태가 아닌 경우")
    void processConfirm_orderNotCompleted() {
        // given
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");

        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment payment = Payment.register(order.getId(), 100000L);
        payment.updateComplete();
        paymentRepository.save(payment);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(payment.getId(), member.getId())
        ).isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("구매 확정 실패 - 이미 확정 된 주문 재접근")
    void processConfirm_alreadyConfirmed() {
        // given - 이미 CONFIRMED 상태
        Member member = createMember();
        Order order = Order.create(
                member.getId(), null,
                UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                0L, 100000L, 0L, 100000L, "testAddress1", "testAddress2");
        order.markCompleted();
        order.markConfirmed(); // 이미 확정
        orderRepository.save(order);
        orderIdList.add(order.getId());

        Payment payment = Payment.register(order.getId(), 100000L);
        payment.updateComplete();
        paymentRepository.save(payment);

        // when & then
        assertThatThrownBy(() ->
                paymentService.processConfirm(payment.getId(), member.getId())
        ).isInstanceOf(ServiceErrorException.class);
    }
}
