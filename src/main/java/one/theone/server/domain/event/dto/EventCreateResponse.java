package one.theone.server.domain.event.dto;

public record EventCreateResponse(
        Long eventId,
        Long eventRewardId
) {
}
