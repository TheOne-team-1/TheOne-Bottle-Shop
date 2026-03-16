package one.theone.server.domain.coupon.repository;

import one.theone.server.domain.coupon.dto.response.CouponDetailResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchMeResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponQueryRepository {
    Page<CouponSearchResponse> findAllCoupons(Coupon.CouponUseType useType, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable);
    Optional<CouponDetailResponse> findCouponDetail(Long couponId);
    Page<CouponSearchMeResponse> findMyCoupons(Long memberId, MemberCoupon.MemberCouponStatus status, Pageable pageable);
}
