package one.theone.server.domain.event.dto;

import one.theone.server.domain.event.entity.Event;

import java.time.LocalDateTime;

public record EventsGetRequest(
        Event.EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
