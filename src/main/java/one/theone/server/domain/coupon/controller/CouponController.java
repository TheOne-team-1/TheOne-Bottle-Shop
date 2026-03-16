package one.theone.server.domain.coupon.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.coupon.dto.response.CouponDetailResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchMeResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.service.CouponService;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/api/admin/coupons")
    public ResponseEntity<BaseResponse<PageResponse<CouponSearchResponse>>> getCoupons(
            @RequestParam(required = false) Coupon.CouponUseType useType
            , @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt
            , @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
            , @RequestParam(defaultValue = "1") int page
            , @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "쿠폰 목록 조회 성공",
                        couponService.getCoupons(useType, startAt, endAt, PageRequest.of(page - 1, size))));
    }

    @GetMapping("/api/admin/coupons/{couponId}")
    public ResponseEntity<BaseResponse<CouponDetailResponse>> getCouponDetails(
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "쿠폰 세부내역 조회 성공",
                        couponService.getCouponDetails(couponId)));
    }

    @GetMapping("/api/coupons/me")
    public ResponseEntity<BaseResponse<PageResponse<CouponSearchMeResponse>>> getMyCoupons(
            @RequestParam(required = false) MemberCoupon.MemberCouponStatus status
            , @RequestParam(defaultValue = "1") int page
            , @RequestParam(defaultValue = "10") int size
            , @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "내 쿠폰 조회 성공",
                        couponService.getMyCoupons(memberId, status, PageRequest.of(page - 1, size))));
    }
}
