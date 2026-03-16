package one.theone.server.domain.coupon.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.coupon.dto.response.CouponSearchResponse;
import one.theone.server.domain.coupon.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static one.theone.server.domain.coupon.entity.QCoupon.coupon;

@Repository
@RequiredArgsConstructor
public class CouponQueryRepositoryImpl implements CouponQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CouponSearchResponse> findAllCoupons(Coupon.CouponUseType useType, LocalDateTime startAt, LocalDateTime endAt, Pageable pageable) {
        List<CouponSearchResponse> content = queryFactory
                .select(Projections.constructor(CouponSearchResponse.class,
                        coupon.id,
                        coupon.name,
                        coupon.useType,
                        coupon.minPrice,
                        coupon.discountValue,
                        coupon.availQuantity.add(coupon.issuedQuantity),
                        coupon.issuedQuantity,
                        coupon.availQuantity,
                        coupon.startAt,
                        coupon.endAt
                ))
                .from(coupon)
                .where(
                        useTypeEq(useType),
                        startAtGoe(startAt),
                        endAtLoe(endAt)
                )
                .orderBy(coupon.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(coupon.updatedAt.desc())
                .fetch();

        Long total = queryFactory
                .select(coupon.count())
                .from(coupon)
                .where(
                        useTypeEq(useType),
                        startAtGoe(startAt),
                        endAtLoe(endAt)
                )
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression useTypeEq(Coupon.CouponUseType useType) {
        return useType != null ? coupon.useType.eq(useType) : null;
    }

    private BooleanExpression startAtGoe(LocalDateTime startAt) {
        return startAt != null ? coupon.startAt.goe(startAt) : null;
    }

    private BooleanExpression endAtLoe(LocalDateTime endAt) {
        return endAt != null ? coupon.endAt.loe(endAt) : null;
    }
}
