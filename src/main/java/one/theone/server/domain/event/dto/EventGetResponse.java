package one.theone.server.domain.event.dto;

import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventReward;

import java.time.LocalDateTime;

public record EventGetResponse(
        Long eventId,
        String name,
        Event.EventType type,
        Event.EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        EventDetailGetResponse details,
        EventRewardGetResponse reward
) {
    public record EventDetailGetResponse(
            Long eventProductId,
            Long minPrice
    ) {}
    public record EventRewardGetResponse(
            EventReward.EventRewardType rewardType,
            Long couponId,
            Long freebieId,
            String name,
            Long availQuantity,
            Long issuedQuantity
    ) {}
}
