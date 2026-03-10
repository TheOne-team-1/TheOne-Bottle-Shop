package one.theone.server.domain.order.dto.response;

import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;

import java.math.BigDecimal;

public record OrderCreateResponse(
        Long orderId,
        String orderNum,
        OrderStatus status,
        BigDecimal usedPoint,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount
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
