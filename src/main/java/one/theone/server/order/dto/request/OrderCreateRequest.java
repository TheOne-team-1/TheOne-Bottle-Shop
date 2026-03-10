package one.theone.server.order.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreateRequest(
        Long memberId,
        Long memberCouponId,
        BigDecimal usedPoint,
        String memberAddressSnap,
        String memberAddressDetailSnap,
        List<OrderItemRequest> orderItems
) {
    public record OrderItemRequest(
            Long productId,
            String productNameSnap,
            BigDecimal productPriceSnap,
            Integer quantity
    ) {
    }
}
