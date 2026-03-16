package one.theone.server.domain.event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventReward;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

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
    @JsonInclude(NON_NULL)
    public record EventDetailGetResponse(
            Long eventProductId,
            Long minPrice
    ) {}
    @JsonInclude(NON_NULL)
    public record EventRewardGetResponse(
            EventReward.EventRewardType rewardType,
            Long couponId,
            Long freebieId,
            String name,
            Long availQuantity,
            Long issuedQuantity
    ) {}
}
