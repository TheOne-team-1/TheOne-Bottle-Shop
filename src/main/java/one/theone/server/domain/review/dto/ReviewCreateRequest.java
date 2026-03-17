package one.theone.server.domain.review.dto;

import jakarta.validation.constraints.*;

public record ReviewCreateRequest(
        @NotNull(message = "주문 상세 ID는 필수입니다.")
        Long orderDetailId,

        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 최대 5점 이하여야 합니다.")
        int rating,

        @NotBlank(message = "리뷰 제목을 입력해주세요.")
        String title,

        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        String content
) {}


