package one.theone.server.domain.event.dto;

import java.time.LocalDateTime;

public record EventDeleteResponse(
        Long eventId,
        String name,
        Boolean deleted,
        LocalDateTime deletedAt
) {
}
