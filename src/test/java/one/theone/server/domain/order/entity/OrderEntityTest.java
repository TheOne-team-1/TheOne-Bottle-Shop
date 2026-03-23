package one.theone.server.domain.order.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityTest {

    @Test
    @DisplayName("주문 생성 시 기본 상태는 PENDING_PAYMENT다")
    void create_setsPendingPayment() {
        Order order = Order.create(
                1L,
                2L,
                "20260318-00000001",
                1000L,
                20000L,
                0L,
                19000L,
                "서울시",
                "101동"
        );

        assertThat(order.getMemberId()).isEqualTo(1L);
        assertThat(order.getMemberCouponId()).isEqualTo(2L);
        assertThat(order.getOrderNum()).isEqualTo("20260318-00000001");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("주문 상태를 취소로 변경할 수 있다")
    void markCancelled() {
        Order order = Order.create(1L, null, "20260318-00000001", 0L, 10000L, 0L, 10000L, "서울시", "101동");

        order.markCancelled();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("주문 상태를 완료로 변경할 수 있다")
    void markCompleted() {
        Order order = Order.create(1L, null, "20260318-00000001", 0L, 10000L, 0L, 10000L, "서울시", "101동");

        order.markCompleted();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("주문 상태를 확정으로 변경할 수 있다")
    void markConfirmed() {
        Order order = Order.create(1L, null, "20260318-00000001", 0L, 10000L, 0L, 10000L, "서울시", "101동");

        order.markConfirmed();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("할인 금액을 반영해 최종 금액을 계산한다")
    void calculateFinalAmount() {
        Order order = Order.create(1L, null, "20260318-00000001", 1000L, 10000L, 0L, 9000L, "서울시", "101동");

        order.calculateFinalAmount(2000L);

        assertThat(order.getDiscountAmount()).isEqualTo(2000L);
        assertThat(order.getFinalAmount()).isEqualTo(7000L);
    }
}