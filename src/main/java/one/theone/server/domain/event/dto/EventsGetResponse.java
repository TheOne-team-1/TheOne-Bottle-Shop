package one.theone.server.domain.event.dto;

import one.theone.server.domain.event.entity.Event;

import java.time.LocalDateTime;

public record EventsGetResponse(
        Long eventId,
        String name,
        Event.EventType type,
        Event.EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
