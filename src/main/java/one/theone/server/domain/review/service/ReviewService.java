package one.theone.server.domain.review.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.annotation.RedisLock;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.common.exception.domain.ReviewExceptionEnum;
import one.theone.server.domain.point.event.PointEarnPublisher;
import one.theone.server.domain.point.event.RedisPointEarnEvent;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.review.dto.*;
import one.theone.server.domain.review.entity.Review;
import one.theone.server.domain.review.repository.ReviewRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PointEarnPublisher pointEarnPublisher;
    private final ProductRepository productRepository;


    @CacheEvict(value = "reviewList", allEntries = true)
    @Transactional
    public BaseResponse<Void> createReview(ReviewCreateRequest request, Long memberId) {
        if (reviewRepository.existsByOrderDetailIdAndDeletedFalse(request.orderDetailId())) {
            throw new ServiceErrorException(ReviewExceptionEnum.ALREADY_REVIEWED);
        }
        Review review = Review.create(request.orderDetailId(), memberId, request.productId(), request.rating(), "리뷰", request.content());
        reviewRepository.save(review);
        pointEarnPublisher.publish(new RedisPointEarnEvent(memberId, 200L,"리뷰 작성 포인트 지급"));
        calculateRating(request.productId());
        return BaseResponse.success("OK", "리뷰 작성 완료", null);
    }

    @RedisLock(key = "'review_like:' + #reviewId")
    public BaseResponse<Void> likeReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ServiceErrorException(ReviewExceptionEnum.REVIEW_NOT_FOUND));
        review.increaseLikeCount();
        return BaseResponse.success("OK", "좋아요 성공", null);
    }

    // [어뷰징 방지] Redis 활용 조회수 중복 체크
    @Transactional
    public BaseResponse<ReviewDetailResponse> getReviewDetail(Long id, Long memberId) {
        Review review = reviewRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ServiceErrorException(ReviewExceptionEnum.REVIEW_NOT_FOUND));

        String viewKey = "view:" + id + ":" + memberId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(viewKey))) {
            review.increaseViewCount();
            redisTemplate.opsForValue().set(viewKey, "true", Duration.ofHours(24));
        }

        ReviewDetailResponse detail = reviewRepository.findReviewDetail(id).get();
        return BaseResponse.success("OK", "조회 성공", detail);
    }

    @Cacheable(
            value = "reviewList",
            key = "{#condition, #pageable.pageNumber}",
            cacheManager = "redisCacheManager"
    )
    public BaseResponse<ReviewListResponse> findReviews(ReviewSearchCondition condition, Pageable pageable) {
        List<ReviewResponse> top3 = reviewRepository.findTop3ByLikes();
        Page<ReviewResponse> allReviews = reviewRepository.findReviewsByCondition(condition, pageable);
        return BaseResponse.success("OK", "목록 조회 성공", ReviewListResponse.of(top3, allReviews));
    }

    @Transactional
    public BaseResponse<Void> deleteReview(Long reviewId, Long memberId, String role) {
        Review review = reviewRepository.findByIdAndDeletedFalse(reviewId)
                .orElseThrow(() -> new ServiceErrorException(ReviewExceptionEnum.REVIEW_NOT_FOUND));
        if (!review.getMemberId().equals(memberId) && !role.equals("ADMIN")) {
            throw new ServiceErrorException(ReviewExceptionEnum.NOT_AUTHORIZED_DELETE);
        }
        review.delete();
        calculateRating(review.getProductId());
        return BaseResponse.success("OK", "삭제 성공", null);
    }

    private void calculateRating(Long productId) {
        BigDecimal avgRating = reviewRepository.findAvgRatingByProductId(productId)
                .orElse(null);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.updateRating(avgRating != null ? avgRating.setScale(1, RoundingMode.HALF_UP) : null);
    }
}