package one.theone.server.domain.point.dto;

import one.theone.server.domain.point.entity.PointLog;

import java.time.LocalDate;

public record PointLogsGetRequest(
        PointLog.PointType type,
        LocalDate startDate,
        LocalDate endDate
) {}
