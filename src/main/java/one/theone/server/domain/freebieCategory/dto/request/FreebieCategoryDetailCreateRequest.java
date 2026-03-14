package one.theone.server.domain.freebieCategory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FreebieCategoryDetailCreateRequest(
        @NotNull(message = "사은품 카테고리 ID는 필수입니다")
        Long freebieCategoryId,

        @NotBlank(message = "사은품 세부 카테고리 이름은 필수입니다")
        String name,

        @Positive(message = "정렬 순서는 0보다 커야 합니다")
        Integer sortNum
) {}
