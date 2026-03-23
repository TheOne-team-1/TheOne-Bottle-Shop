package one.theone.server.domain.point.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PointAdjustRequest(
        @NotNull(message = "포인트 변동액은 필수입니다")
        Long amount,

        @NotBlank(message = "설명은 필수입니다")
        String description
) {}
