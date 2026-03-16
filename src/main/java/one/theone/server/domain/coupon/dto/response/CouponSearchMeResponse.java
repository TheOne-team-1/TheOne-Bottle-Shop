package one.theone.server.domain.coupon.dto.response;

import one.theone.server.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponSearchMeResponse(
        Long couponId
        , String name
        , Coupon.CouponUseType useType
        , Long minPrice
        , Long discountValue
        , LocalDateTime startAt
        , LocalDateTime endAt
) {}
