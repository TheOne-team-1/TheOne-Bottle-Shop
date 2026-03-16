package one.theone.server.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CouponExceptionEnum;
import one.theone.server.domain.coupon.dto.response.CouponDetailResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchMeResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
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

    @Transactional(readOnly = true)
    public CouponDetailResponse getCouponDetails(Long couponId) {
        return couponRepository.findCouponDetail(couponId).orElseThrow(() -> new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponSearchMeResponse> getMyCoupons(MemberCoupon.MemberCouponStatus status, Pageable pageable) {
        Page<CouponSearchMeResponse> page = couponRepository.findMyCoupons(status, pageable);
        return PageResponse.register(page);
    }
}
