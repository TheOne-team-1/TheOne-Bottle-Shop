package one.theone.server.domain.event.repository;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.event.dto.EventGetResponse;
import one.theone.server.domain.event.dto.EventsGetRequest;
import one.theone.server.domain.event.dto.EventsGetResponse;
import one.theone.server.domain.event.entity.Event;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventQueryRepository {

    PageResponse<EventsGetResponse> findEventsWithConditions(
            EventsGetRequest request, Pageable pageable, List<Event.EventStatus> statuses, boolean isAdmin);

    EventGetResponse findEventInfoById(Long eventId, boolean isAdmin);
}
