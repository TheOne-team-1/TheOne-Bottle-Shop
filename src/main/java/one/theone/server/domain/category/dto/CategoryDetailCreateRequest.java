package one.theone.server.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CategoryDetailCreateRequest(
        @NotNull(message = "대분류 카테고리 아이디는 필수입니다")
        Long categoryId,

        @NotBlank(message = "카테고리 이름은 필수입니다")
        String name,

        @Positive(message = "정렬 순서는 0보다 커야 합니다")
        Integer sortNum
) {}
