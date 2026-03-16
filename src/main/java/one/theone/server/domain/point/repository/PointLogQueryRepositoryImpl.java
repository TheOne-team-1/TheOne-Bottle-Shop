package one.theone.server.domain.point.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.point.dto.PointLogsGetRequest;
import one.theone.server.domain.point.dto.PointLogsGetResponse;
import one.theone.server.domain.point.entity.PointLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static one.theone.server.domain.point.entity.QPointLog.pointLog;

@RequiredArgsConstructor
public class PointLogQueryRepositoryImpl implements PointLogQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PointLogsGetResponse> findPointLogs(Long memberId, PointLogsGetRequest request, Pageable pageable) {
        List<PointLogsGetResponse> result = queryFactory
                .select(Projections.constructor(PointLogsGetResponse.class,
                        pointLog.id,
                        pointLog.type,
                        pointLog.amount,
                        pointLog.balanceSnap,
                        pointLog.orderId,
                        pointLog.createdAt,
                        pointLog.expiresAt))
                .from(pointLog)
                .where(
                        pointLog.memberId.eq(memberId),
                        typeEq(request.type()),
                        dateBetween(request.startDate(), request.endDate())
                )
                .orderBy(pointLog.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(pointLog.count())
                .from(pointLog)
                .where(
                        pointLog.memberId.eq(memberId),
                        typeEq(request.type()),
                        dateBetween(request.startDate(), request.endDate())
                )
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(result, pageable, total);
    }

    private BooleanExpression typeEq(PointLog.PointType type) {
        return type != null ? pointLog.type.eq(type) : null;
    }

    private BooleanExpression dateBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atTime(0, 0, 0) : LocalDateTime.now().minusMonths(6);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        return pointLog.createdAt.between(start, end);
    }
}
