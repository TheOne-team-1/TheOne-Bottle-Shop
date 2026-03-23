package one.theone.server.domain.coupon.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import one.theone.server.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponDetailResponse(
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
        , @JsonProperty("member_issue_stat")
        MemberIssueStat memberIssueStat
) {
    public record MemberIssueStat(
            long available,
            long used,
            long expired
    ) {}
}
