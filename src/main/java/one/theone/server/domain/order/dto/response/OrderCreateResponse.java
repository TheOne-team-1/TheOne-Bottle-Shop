package one.theone.server.domain.order.dto.response;

import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;

public record OrderCreateResponse(
        Long orderId,
        String orderNum,
        OrderStatus status,
        Long usedPoint,
        Long totalAmount,
        Long discountAmount,
        Long finalAmount
) {
    public static OrderCreateResponse from(Order order) {
        return new OrderCreateResponse(
                order.getId(),
                order.getOrderNum(),
                order.getStatus(),
                order.getUsedPoint(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getFinalAmount()
        );
    }
}
