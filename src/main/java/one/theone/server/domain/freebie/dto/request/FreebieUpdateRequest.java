package one.theone.server.domain.freebie.dto.request;

import jakarta.validation.constraints.Min;

public record FreebieUpdateRequest(
        Long freebieCategoryDetailId,
        String name,

        @Min(value = 0, message = "수량은 0 이상이어야 합니다")
        Long quantity
) {}
