package one.theone.server.domain.coupon.dto.response;

import one.theone.server.domain.coupon.entity.MemberCoupon;

public record CouponIssueResponse(
        Long memberCouponId,
        Long memberId,
        Long couponId,
        MemberCoupon.MemberCouponIssueWay issueWay
) {}
