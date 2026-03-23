package one.theone.server.domain.order.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDetailEntityTest {

    @Test
    @DisplayName("주문 상세 생성 시 라인 금액을 계산한다")
    void create_calculatesLineAmount() {
        OrderDetail detail = OrderDetail.create(
                1L,
                10L,
                "와인",
                10000L,
                3
        );

        assertThat(detail.getOrderId()).isEqualTo(1L);
        assertThat(detail.getProductId()).isEqualTo(10L);
        assertThat(detail.getProductNameSnap()).isEqualTo("와인");
        assertThat(detail.getProductPriceSnap()).isEqualTo(10000L);
        assertThat(detail.getQuantity()).isEqualTo(3);
        assertThat(detail.getLineAmount()).isEqualTo(30000L);
    }
}