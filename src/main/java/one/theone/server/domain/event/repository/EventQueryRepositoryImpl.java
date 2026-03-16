package one.theone.server.domain.event.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.event.dto.EventsGetResponse;
import one.theone.server.domain.event.entity.Event;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public PageResponse<EventsGetResponse> findEventsWithConditions(Pageable pageable, List<Event.EventStatus> statuses) {
        return null;
    }
}
