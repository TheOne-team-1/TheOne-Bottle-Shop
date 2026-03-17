package one.theone.server.domain.order.dto.request;

public record OrderCreateDirectRequest(
        Long memberId,
        Long productId,
        Integer quantity,
        Long memberCouponId,
        Long usedPoint,
        String memberAddressSnap,
        String memberAddressDetailSnap
) {
}
