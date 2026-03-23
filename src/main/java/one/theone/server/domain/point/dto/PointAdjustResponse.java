package one.theone.server.domain.point.dto;

public record PointAdjustResponse(
        Long memberId,
        String description,
        Long amount,
        Long balance
) {}
