package one.theone.server.domain.coupon.dto.response;

import one.theone.server.domain.coupon.entity.MemberCoupon;

public record CouponRecallResponse(
        Long memberId
        , Long memberCouponId
        , MemberCoupon.MemberCouponStatus status
) {
}
