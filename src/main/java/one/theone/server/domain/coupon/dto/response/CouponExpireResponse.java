package one.theone.server.domain.coupon.dto.response;

public record CouponExpireResponse(
        Long couponId,
        int expiredCount
) {
}
