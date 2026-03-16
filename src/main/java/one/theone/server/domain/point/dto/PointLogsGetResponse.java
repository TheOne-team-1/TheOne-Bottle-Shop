package one.theone.server.domain.point.dto;

import one.theone.server.domain.point.entity.PointLog;

import java.time.LocalDateTime;

public record PointLogsGetResponse(
        Long id,
        PointLog.PointType type,
        Long amount,
        Long balanceSnap,
        Long orderId,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}
