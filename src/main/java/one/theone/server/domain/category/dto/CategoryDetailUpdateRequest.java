package one.theone.server.domain.category.dto;

import jakarta.validation.constraints.Positive;

public record CategoryDetailUpdateRequest(
        Long categoryId,
        String name,

        @Positive(message = "정렬 순서는 0보다 커야 합니다")
        Integer sortNum
) {}