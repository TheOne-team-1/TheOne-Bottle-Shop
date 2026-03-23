package one.theone.server.domain.review.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ReviewListResponse(
        List<ReviewResponse> top3Reviews, // 상위 3개 고정
        List<ReviewResponse> allReviews,
        int currentPage,
        long totalElements,
        int totalPages
) {
    public static ReviewListResponse of(List<ReviewResponse> top3, Page<ReviewResponse> page) {
        return new ReviewListResponse(
                top3,
                page.getContent(),
                page.getNumber() + 1,
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
