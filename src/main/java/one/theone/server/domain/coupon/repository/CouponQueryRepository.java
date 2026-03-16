package one.theone.server.domain.coupon.repository;

import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface CouponQueryRepository {
    Page<CouponSearchResponse> findAllCoupons(Coupon.CouponUseType useType, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable);
}
