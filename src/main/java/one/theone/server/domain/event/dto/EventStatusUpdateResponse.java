package one.theone.server.domain.event.dto;

public record EventStatusUpdateResponse(
        Long eventId,
        String name
) {
}
