package one.theone.server.domain.event.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.event.dto.EventsGetResponse;
import one.theone.server.domain.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static one.theone.server.domain.event.entity.QEvent.event;

@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public PageResponse<EventsGetResponse> findEventsWithConditions(Pageable pageable, List<Event.EventStatus> statuses) {
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
                        event.status.in(statuses)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(event.count())
                .from(event)
                .where(
                        event.deleted.isFalse(),
                        event.status.in(statuses)
                );

        Page<EventsGetResponse> page = PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

        return PageResponse.register(page);
    }
}
