package one.theone.server.domain.event.dto;

import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventReward;

public record EventCreateResponse(
        Long eventId,
        Long eventRewardId
) {
    public static EventCreateResponse from(Event event, EventReward reward) {
        return new EventCreateResponse(
                event.getId(),
                reward.getId()
        );
    }
}
