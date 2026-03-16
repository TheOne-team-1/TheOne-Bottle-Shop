package one.theone.server.domain.point.dto;

import one.theone.server.domain.point.entity.PointLog;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PointLogsGetResponse(
        Long id,
        PointLog.PointType type,
        String description,
        Long amount,
        Long balanceSnap,
        Long orderId,
        LocalDateTime createdAt,
        LocalDate expiresAt
) {}
