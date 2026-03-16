package one.theone.server.domain.point.dto;

import jakarta.validation.constraints.NotNull;

public record PointAdjustRequest(
        @NotNull(message = "포인트 변동액은 필수입니다")
        Long amount
) {}
