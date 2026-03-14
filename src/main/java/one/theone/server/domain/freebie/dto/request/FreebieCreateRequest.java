package one.theone.server.domain.freebie.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FreebieCreateRequest(
        @NotNull(message = "사은품 세부 카테고리 ID는 필수입니다")
        Long freebieCategoryDetailId,

        @NotBlank(message = "사은품 이름은 필수입니다")
        String name,

        @NotNull(message = "수량은 필수입니다")
        @Min(value = 0, message = "수량은 0 이상이어야 합니다")
        Long quantity
) {}
