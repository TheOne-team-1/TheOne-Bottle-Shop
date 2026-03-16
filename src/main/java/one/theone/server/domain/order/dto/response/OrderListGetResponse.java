package one.theone.server.domain.order.dto.response;

import one.theone.server.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderListGetResponse(
        Long orderId,
        String orderNum,
        OrderStatus status,
        Long finalAmount,
        LocalDateTime createdAt
) {
}
