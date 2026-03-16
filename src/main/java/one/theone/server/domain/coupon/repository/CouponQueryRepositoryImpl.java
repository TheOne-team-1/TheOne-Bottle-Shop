package one.theone.server.domain.coupon.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.coupon.dto.response.CouponDetailResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchMeResponse;
import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static one.theone.server.domain.coupon.entity.QCoupon.coupon;
import static one.theone.server.domain.coupon.entity.QMemberCoupon.memberCoupon;

@Repository
@RequiredArgsConstructor
public class CouponQueryRepositoryImpl implements CouponQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CouponSearchResponse> findAllCoupons(Coupon.CouponUseType useType, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        List<CouponSearchResponse> content = queryFactory
                .select(Projections.constructor(CouponSearchResponse.class
                        , coupon.id
                        , coupon.name
                        , coupon.useType
                        , coupon.minPrice
                        , coupon.discountValue
                        , coupon.availQuantity
                        , coupon.issuedQuantity
                        , coupon.availQuantity.subtract(coupon.issuedQuantity)
                        , coupon.startAt
                        , coupon.endAt
                ))
                .from(coupon)
                .where(
                        useTypeEq(useType)
                        , startAtGoe(startAt)
                        , endAtLoe(endAt)
                )
                .orderBy(coupon.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(coupon.count())
                .from(coupon)
                .where(
                        useTypeEq(useType)
                        , startAtGoe(startAt)
                        , endAtLoe(endAt)
                )
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<CouponDetailResponse> findCouponDetail(Long couponId) {
        Coupon result = queryFactory
                .selectFrom(coupon)
                .where(coupon.id.eq(couponId))
                .fetchOne();

        if (result == null) {
            return Optional.empty();
        }

        long available = countByCouponIdAndStatus(couponId, MemberCoupon.MemberCouponStatus.AVAILABLE);
        long used = countByCouponIdAndStatus(couponId, MemberCoupon.MemberCouponStatus.USED);
        long expired = countByCouponIdAndStatus(couponId, MemberCoupon.MemberCouponStatus.EXPIRED);

        CouponDetailResponse.MemberIssueStat memberIssueStat = new CouponDetailResponse.MemberIssueStat(available, used, expired);

        return Optional.of(new CouponDetailResponse(
                result.getId()
                , result.getName()
                , result.getUseType()
                , result.getMinPrice()
                , result.getDiscountValue()
                , result.getAvailQuantity()
                , result.getIssuedQuantity()
                , result.getAvailQuantity() - result.getIssuedQuantity()
                , result.getStartAt()
                , result.getEndAt()
                , memberIssueStat
        ));
    }

    private long countByCouponIdAndStatus(Long couponId, MemberCoupon.MemberCouponStatus status) {
        Long count = queryFactory
                .select(memberCoupon.count())
                .from(memberCoupon)
                .where(
                        memberCoupon.couponId.eq(couponId),
                        memberCoupon.status.eq(status)
                )
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Page<CouponSearchMeResponse> findMyCoupons(Long memberId, MemberCoupon.MemberCouponStatus status, Pageable pageable) {
        List<CouponSearchMeResponse> content = queryFactory
                .select(Projections.constructor(CouponSearchMeResponse.class,
                        coupon.id
                        , coupon.name
                        , coupon.useType
                        , coupon.minPrice
                        , coupon.discountValue
                        , coupon.startAt
                        , coupon.endAt
                ))
                .from(coupon)
                .innerJoin(memberCoupon)
                .on(coupon.id.eq(memberCoupon.couponId))
                .where(
                        userSearchStatusEq(status)
                        , memberCoupon.memberId.eq(memberId)
                )
                .orderBy(coupon.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(memberCoupon.count())
                .from(memberCoupon)
                .where(
                        userSearchStatusEq(status)
                        , memberCoupon.memberId.eq(memberId)
                )
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<MemberCoupon> findExpiredMemberCoupons(LocalDateTime expiredAt) {
        return queryFactory
                .selectFrom(memberCoupon)
                .join(coupon).on(coupon.id.eq(memberCoupon.couponId))
                .where(
                        memberCoupon.status.eq(MemberCoupon.MemberCouponStatus.AVAILABLE)
                        , coupon.endAt.lt(expiredAt)
                        , coupon.deleted.isFalse()
                        , memberCoupon.deleted.isFalse()
                )
                .fetch();
    }

    private BooleanExpression useTypeEq(Coupon.CouponUseType useType) {
        return useType != null ? coupon.useType.eq(useType) : null;
    }

    private BooleanExpression userSearchStatusEq(MemberCoupon.MemberCouponStatus status) {
        return status != null ? memberCoupon.status.eq(status) : memberCoupon.status.notIn(MemberCoupon.MemberCouponStatus.RECALL);
    }

    private BooleanExpression startAtGoe(LocalDateTime startAt) {
        return startAt != null ? coupon.startAt.goe(startAt) : null;
    }

    private BooleanExpression endAtLoe(LocalDateTime endAt) {
        return endAt != null ? coupon.endAt.loe(endAt) : null;
    }
}
