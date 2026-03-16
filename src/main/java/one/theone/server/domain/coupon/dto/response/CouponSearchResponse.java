package one.theone.server.domain.coupon.dto.response;

import one.theone.server.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponSearchResponse(
        Long couponId
        , String name
        , Coupon.CouponUseType useType
        , Long minPrice
        , Long discountValue
        , Long availQuantity
        , Long issuedQuantity
        , Long remainQuantity
        , LocalDateTime startAt
        , LocalDateTime endAt
) {}
