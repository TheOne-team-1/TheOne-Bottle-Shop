package one.theone.server.domain.review.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long orderDetailId,
        String memberName,
        String productName,
        int rating,
        String content,
        LocalDateTime createdAt
) {}