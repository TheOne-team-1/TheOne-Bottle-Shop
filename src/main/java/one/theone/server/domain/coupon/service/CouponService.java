package one.theone.server.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CouponExceptionEnum;
import one.theone.server.domain.coupon.dto.request.CouponCreateRequest;
import one.theone.server.domain.coupon.dto.request.CouponIssueAdminRequest;
import one.theone.server.domain.coupon.dto.response.CouponCreateResponse;
import one.theone.server.domain.coupon.dto.response.CouponDetailResponse;
import one.theone.server.domain.coupon.dto.response.CouponExpireResponse;
import one.theone.server.domain.coupon.dto.response.CouponIssueResponse;
import one.theone.server.domain.coupon.dto.response.CouponRecallResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchMeResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.repository.CouponQueryRepository;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import one.theone.server.domain.member.repository.MemberRepository;

import static one.theone.server.common.exception.domain.CouponExceptionEnum.*;
import static one.theone.server.common.exception.domain.MemberExceptionEnum.ERR_MEMBER_NOT_FOUND;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponQueryRepository couponQueryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CouponCreateResponse createCoupon(CouponCreateRequest request) {
        if(request.useType() == Coupon.CouponUseType.AMOUNT) {
            if(request.discountValue() < 5000) {
                throw new ServiceErrorException(ERR_AMOUNT_COUPON_DISCOUNT_VALUE_MIN);
            }
        }

        if(request.useType() == Coupon.CouponUseType.RATE) {
            if(request.discountValue() > 100) {
                throw new ServiceErrorException(ERR_RATE_COUPON_DISCOUNT_VALUE_MAX);
            }
        }

        if (!request.endAt().isAfter(request.startAt().toLocalDate())) {
            throw new ServiceErrorException(ERR_COUPON_END_AT_BEFORE_START_AT);
        }

        Coupon coupon = Coupon.register(
                request.name()
                , request.useType()
                , request.minPrice()
                , request.discountValue()
                , request.availQuantity()
                , request.startAt()
                , request.endAt()
        );
        couponRepository.save(coupon);
        return new CouponCreateResponse(coupon.getId());
    }

    @Transactional
    public CouponIssueResponse issueCouponByAdmin(Long couponId, CouponIssueAdminRequest request) {
        Coupon coupon = ValidateIssueCoupon(couponId, request.memberId());
        coupon.issueCoupon();
        MemberCoupon memberCoupon = MemberCoupon.issuedByAdmin(request.memberId(), couponId);
        memberCouponRepository.save(memberCoupon);

        return new CouponIssueResponse(
                memberCoupon.getId()
                , memberCoupon.getMemberId()
                , memberCoupon.getCouponId()
                , memberCoupon.getIssueWay()
        );
    }

    @Transactional
    public CouponIssueResponse issueCouponByEvent(Long couponId, Long memberId, Long eventId) {
        Coupon coupon = ValidateIssueCoupon(couponId, memberId);
        coupon.issueCoupon();
        MemberCoupon memberCoupon = MemberCoupon.issuedByEvent(memberId, couponId, eventId);
        memberCouponRepository.save(memberCoupon);

        return new CouponIssueResponse(
                memberCoupon.getId()
                , memberCoupon.getMemberId()
                , memberCoupon.getCouponId()
                , memberCoupon.getIssueWay()
        );
    }

    @Transactional
    public void issueCancelCouponByEvent(Long memberCouponId) {
        MemberCoupon memberCoupon = memberCouponRepository.findByIdAndDeletedFalse(memberCouponId).orElseThrow(() -> new ServiceErrorException(ERR_MEMBER_COUPON_NOT_FOUND));
        Coupon coupon = couponRepository.findById(memberCoupon.getCouponId()).orElseThrow(() -> new ServiceErrorException(ERR_COUPON_NOT_FOUND));

        coupon.issueCancelCoupon();
        memberCoupon.delete();
    }

    private Coupon ValidateIssueCoupon(Long couponId, Long memberId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_NOT_FOUND));

        memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceErrorException(ERR_MEMBER_NOT_FOUND));

        if (memberCouponRepository.existsByMemberIdAndCouponIdAndStatusAndDeletedFalse(
                memberId, couponId, MemberCoupon.MemberCouponStatus.AVAILABLE)) {
            throw new ServiceErrorException(ERR_COUPON_ALREADY_ISSUED);
        }

        return coupon;
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponSearchResponse> getCoupons(Coupon.CouponUseType useType, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        Page<CouponSearchResponse> page = couponQueryRepository.findAllCoupons(useType, startAt, endAt, pageable);
        return PageResponse.register(page);
    }

    @Transactional(readOnly = true)
    public CouponDetailResponse getCouponDetails(Long couponId) {
        return couponQueryRepository.findCouponDetail(couponId).orElseThrow(() -> new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponSearchMeResponse> getMyCoupons(Long memberId, MemberCoupon.MemberCouponStatus status, Pageable pageable) {
        Page<CouponSearchMeResponse> page = couponQueryRepository.findMyCoupons(memberId, status, pageable);
        return PageResponse.register(page);
    }

    @Transactional
    public CouponExpireResponse expireCoupon(Long couponId) {
        couponRepository.findById(couponId)
                .orElseThrow(() -> new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_NOT_FOUND));

        List<MemberCoupon> memberCouponList = memberCouponRepository.findAllByCouponIdAndStatusAndDeletedFalse(
                couponId, MemberCoupon.MemberCouponStatus.AVAILABLE);
        memberCouponList.forEach(memberCoupon -> memberCoupon.expireCoupon());

        return new CouponExpireResponse(couponId, memberCouponList.size());
    }

    @Transactional
    public CouponRecallResponse recallCoupon(Long memberId, Long memberCouponId) {
        MemberCoupon memberCoupon = memberCouponRepository.findByIdAndMemberIdAndDeletedFalse(memberCouponId, memberId)
                .orElseThrow(() -> new ServiceErrorException(CouponExceptionEnum.ERR_MEMBER_COUPON_NOT_FOUND));

        memberCoupon.recallByAdmin();

        return new CouponRecallResponse(memberId, memberCouponId, memberCoupon.getStatus());
    }
}
