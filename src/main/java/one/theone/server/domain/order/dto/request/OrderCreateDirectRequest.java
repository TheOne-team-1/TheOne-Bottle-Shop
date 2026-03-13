package one.theone.server.domain.order.dto.request;

import java.util.List;

public record OrderCreateDirectRequest(
        Long memberId,
        Long memberCouponId,
        Long usedPoint,
        String memberAddressSnap,
        String memberAddressDetailSnap,
        List<OrderItemRequest> orderItems
) {
    public record OrderItemRequest(
            Long productId,
            String productNameSnap,
            Long productPriceSnap,
            Integer quantity
    ) {
    }
}
