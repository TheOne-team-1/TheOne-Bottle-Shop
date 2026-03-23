package one.theone.server.domain.order.dto.response;

import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderListGetResponse(
        Long orderId,
        String orderNum,
        OrderStatus status,
        Long finalAmount,
        LocalDateTime createdAt
) {
    public static OrderListGetResponse from(Order order) {
        return new OrderListGetResponse(
                order.getId(),
                order.getOrderNum(),
                order.getStatus(),
                order.getFinalAmount(),
                order.getCreatedAt()
        );
    }
}
