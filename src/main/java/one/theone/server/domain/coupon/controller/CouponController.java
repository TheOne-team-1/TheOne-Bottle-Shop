package one.theone.server.domain.coupon.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.coupon.dto.request.CouponCreateRequest;
import one.theone.server.domain.coupon.dto.request.CouponIssueEventRequest;
import one.theone.server.domain.coupon.dto.request.CouponIssueAdminRequest;
import one.theone.server.domain.coupon.dto.response.*;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.service.CouponService;
import one.theone.server.domain.coupon.service.CouponIssueService;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponIssueService couponIssueService;

    @PostMapping("/api/admin/coupons")
    public ResponseEntity<BaseResponse<CouponCreateResponse>> createCoupon(
            @Valid @RequestBody CouponCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "쿠폰 생성 성공",
                        couponService.createCoupon(request)));
    }

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

    @PostMapping("/api/admin/coupons/{couponId}/issue")
    public ResponseEntity<BaseResponse<CouponIssueResponse>> issueCouponByAdmin(
            @PathVariable Long couponId,
            @Valid @RequestBody CouponIssueAdminRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "쿠폰 발급 성공",
                        couponService.issueCouponByAdmin(couponId, request)));
    }

    @PostMapping("/api/coupons/{couponId}/issue")
    public ResponseEntity<BaseResponse<CouponIssueResponse>> issueCouponByEvent(
            @PathVariable Long couponId,
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CouponIssueEventRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "쿠폰 발급 성공",
                        couponIssueService.issueCouponWithLock(couponId, memberId, request.eventId())));
    }

    @PatchMapping("/api/admin/member/{memberId}/coupons/{memberCouponId}/recall")
    public ResponseEntity<BaseResponse<CouponRecallResponse>> recallCoupon(
            @PathVariable Long memberId,
            @PathVariable Long memberCouponId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "쿠폰 회수 성공",
                        couponService.recallCoupon(memberId, memberCouponId)));
    }
}
