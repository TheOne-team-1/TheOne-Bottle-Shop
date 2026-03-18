package one.theone.server.domain.review.dto;

import java.time.LocalDateTime;

public record ReviewDetailResponse(
        Long id,
        String productName,
        String memberName,
        String memberEmail,
        LocalDateTime createdAt,
        int rating,
        int viewCount,
        String content
) {
    public static ReviewDetailResponse from(one.theone.server.domain.review.entity.Review review, String email, String memberName, String productName) {
        return new ReviewDetailResponse(
                review.getId(), productName, memberName, email,
                review.getCreatedAt(), review.getRating(), review.getViewCount(), review.getContent()
        );
    }
}

