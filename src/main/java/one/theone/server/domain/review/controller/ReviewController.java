package one.theone.server.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.review.dto.ReviewDetailResponse;
import one.theone.server.domain.review.dto.ReviewListResponse;
import one.theone.server.domain.review.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import one.theone.server.domain.review.dto.ReviewCreateRequest;
import one.theone.server.domain.review.dto.ReviewSearchCondition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public BaseResponse<Void> create(@Valid @RequestBody ReviewCreateRequest request, @AuthenticationPrincipal Long memberId) {
        return reviewService.createReview(request, memberId);
    }

    @GetMapping
    public BaseResponse<ReviewListResponse> list(@Valid ReviewSearchCondition condition, @PageableDefault(size = 10) Pageable pageable) {
        return reviewService.findReviews(condition, pageable);
    }

    @GetMapping("/{id}")
    public BaseResponse<ReviewDetailResponse> detail(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return reviewService.getReviewDetail(id, memberId);
    }

    @PostMapping("/{id}/like")
    public BaseResponse<Void> like(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return reviewService.likeReview(id, memberId);
    }

    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal Long memberId, @AuthenticationPrincipal String role) {
        return reviewService.deleteReview(id, memberId, role);
    }
}
