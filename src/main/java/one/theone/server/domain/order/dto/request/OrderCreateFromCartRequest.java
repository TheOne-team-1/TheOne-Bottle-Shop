package one.theone.server.domain.order.dto.request;

public record OrderCreateFromCartRequest(
        Long memberId,
        Long memberCouponId,
        Long usedPoint,
        String memberAddressSnap,
        String memberAddressDetailSnap
) {
}
