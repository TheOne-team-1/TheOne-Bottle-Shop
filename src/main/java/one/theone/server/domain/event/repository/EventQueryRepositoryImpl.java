package one.theone.server.domain.event.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.event.dto.EventGetResponse;
import one.theone.server.domain.event.dto.EventsGetRequest;
import one.theone.server.domain.event.dto.EventsGetResponse;
import one.theone.server.domain.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDateTime;
import java.util.List;

import static one.theone.server.domain.event.entity.QEvent.event;

@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public PageResponse<EventsGetResponse> findEventsWithConditions(
            EventsGetRequest request, Pageable pageable, List<Event.EventStatus> statuses, boolean isAdmin) {
        List<EventsGetResponse> content = queryFactory
                .select(Projections.constructor(EventsGetResponse.class,
                        event.id,
                        event.name,
                        event.type,
                        event.status,
                        event.startAt,
                        event.endAt))
                .from(event)
                .where(
                        event.deleted.isFalse(),
                        event.status.in(statuses),
                        startAtLoe(request.endAt()),
                        endAtGoe(request.startAt()),
                        isAdmin ? null : userVisibleCondition()
                )
                .orderBy(event.startAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(event.count())
                .from(event)
                .where(
                        event.deleted.isFalse(),
                        event.status.in(statuses),
                        startAtLoe(request.endAt()),
                        endAtGoe(request.startAt()),
                        isAdmin ? null : userVisibleCondition()
                );

        Page<EventsGetResponse> page = PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

        return PageResponse.register(page);
    }

    @Override
    public EventGetResponse findEventInfoById(Long eventId, boolean isAdmin) {
        return null;
    }

    private BooleanExpression startAtLoe(LocalDateTime endAt) {
        return endAt != null ? event.startAt.loe(endAt) : null;
    }

    private BooleanExpression endAtGoe(LocalDateTime startAt) {
        return startAt != null ? event.endAt.goe(startAt) : null;
    }

    private BooleanExpression userVisibleCondition() {
        LocalDateTime now = LocalDateTime.now();

        BooleanExpression inProgress = event.startAt.loe(now).and(event.endAt.goe(now));
        BooleanExpression comingSoon = event.startAt.between(now, now.plusDays(7));
        BooleanExpression recentlyClosed = event.endAt.between(now.minusDays(7), now);

        return inProgress.or(comingSoon).or(recentlyClosed);
    }
}
