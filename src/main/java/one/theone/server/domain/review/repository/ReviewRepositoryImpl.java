package one.theone.server.domain.review.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.review.dto.ReviewDetailResponse;
import one.theone.server.domain.review.dto.ReviewResponse;
import one.theone.server.domain.review.dto.ReviewSearchCondition;
import one.theone.server.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import java.util.List;
import java.util.Optional;

import static one.theone.server.domain.review.entity.QReview.review; // Q클래스 임포트
import static one.theone.server.domain.product.entity.QProduct.product;
import static one.theone.server.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ReviewResponse> findReviewsByCondition(ReviewSearchCondition condition, Pageable pageable) {
        List<ReviewResponse> content = queryFactory
                .select(Projections.constructor(ReviewResponse.class,
                        review.id, review.orderDetailId, member.name, product.name,
                        review.rating, review.content, review.likeCount, review.viewCount, review.createdAt))
                .from(review)
                .leftJoin(member).on(review.memberId.eq(member.id))
                .leftJoin(product).on(review.productId.eq(product.id))
                .where(keywordContains(condition.keyword()), ratingEq(condition.ratingFilter()), review.deleted.isFalse())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getSortOrder(condition))
                .fetch();

        long total = queryFactory.select(review.count()).from(review).where(review.deleted.isFalse()).fetchOne();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<ReviewResponse> findTop3ByLikes() {
        return queryFactory
                .select(Projections.constructor(ReviewResponse.class,
                        review.id, review.orderDetailId, member.name, product.name,
                        review.rating, review.content, review.likeCount, review.viewCount, review.createdAt))
                .from(review)
                .leftJoin(member).on(review.memberId.eq(member.id))
                .leftJoin(product).on(review.productId.eq(product.id))
                .where(review.deleted.isFalse())
                .orderBy(review.likeCount.desc(), review.createdAt.desc())
                .limit(3)
                .fetch();
    }

    @Override
    public Optional<ReviewDetailResponse> findReviewDetail(Long id) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(ReviewDetailResponse.class,
                        review.id, product.name, member.name, member.email, review.createdAt, review.rating, review.viewCount, review.content))
                .from(review)
                .leftJoin(member).on(review.memberId.eq(member.id))
                .leftJoin(product).on(review.productId.eq(product.id))
                .where(review.id.eq(id), review.deleted.isFalse())
                .fetchOne());
    }

    private OrderSpecifier<?> getSortOrder(ReviewSearchCondition condition) {
        boolean isAsc = "asc".equalsIgnoreCase(condition.sortOrder());
        return switch (condition.sortBy() != null ? condition.sortBy() : "createdAt") {
            case "rating" -> isAsc ? review.rating.asc() : review.rating.desc();
            case "likes" -> isAsc ? review.likeCount.asc() : review.likeCount.desc();
            default -> isAsc ? review.createdAt.asc() : review.createdAt.desc();
        };
    }

    private BooleanExpression keywordContains(String keyword) {
        return keyword == null ? null : member.name.contains(keyword).or(product.name.contains(keyword));
    }

    private BooleanExpression ratingEq(Integer rating) {
        return rating == null ? null : review.rating.eq(rating);
    }
}
