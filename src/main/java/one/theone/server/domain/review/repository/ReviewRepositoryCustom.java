package one.theone.server.domain.review.repository;

import one.theone.server.domain.review.dto.ReviewDetailResponse;
import one.theone.server.domain.review.dto.ReviewResponse;
import one.theone.server.domain.review.dto.ReviewSearchCondition;
import one.theone.server.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewRepositoryCustom {
    Page<ReviewResponse> findReviewsByCondition(ReviewSearchCondition condition, Pageable pageable);
    List<ReviewResponse> findTop3ByLikes(); // 상위 3개 노출 요구사항
    Optional<ReviewDetailResponse> findReviewDetail(Long id);
    List<ReviewResponse> findTop3ByProductIdAndLikes(Long productId);
}
