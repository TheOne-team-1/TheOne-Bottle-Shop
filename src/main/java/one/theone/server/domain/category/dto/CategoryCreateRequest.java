package one.theone.server.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CategoryCreateRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다")
        String name,

        @Positive(message = "정렬 순서는 0보다 커야 합니다")
        Integer sortNum
) {}
