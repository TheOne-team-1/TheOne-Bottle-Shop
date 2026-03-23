package one.theone.server.domain.order.dto.response;

import one.theone.server.domain.order.entity.OrderStatus;

public record OrderCancelResponse(
        Long orderId,
        OrderStatus status
) {
}
