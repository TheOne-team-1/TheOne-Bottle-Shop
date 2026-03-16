package one.theone.server.domain.order.dto.response;

import one.theone.server.domain.order.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailGetResponse(
        Long orderId,
        String orderNum,
        OrderStatus status,
        Long usedPoint,
        Long totalAmount,
        Long discountAmount,
        Long finalAmount,
        String memberAddressSnap,
        String memberAddressDetailSnap,
        LocalDateTime createdAt,
        List<OrderDetailItemResponse> orderItems
) {
    public record OrderDetailItemResponse(
            Long orderDetailId,
            Long productId,
            String productNameSnap,
            Long productPriceSnap,
            Integer quantity,
            Long lineAmount
    ) {
    }
}
