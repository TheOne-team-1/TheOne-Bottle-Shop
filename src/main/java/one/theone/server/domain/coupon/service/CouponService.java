package one.theone.server.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public PageResponse<CouponSearchResponse> getCoupons(Coupon.CouponUseType useType, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        Page<CouponSearchResponse> page = couponRepository.findAllCoupons(useType, startAt, endAt, pageable);
        return PageResponse.register(page);
    }
}
