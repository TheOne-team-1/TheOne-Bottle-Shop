package one.theone.server.domain.point.dto;

public record PointAdjustResponse(
        Long memberId,
        Long amount,
        Long balance
) {}
