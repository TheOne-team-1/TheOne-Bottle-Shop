package one.theone.server.domain.event.dto;

import jakarta.validation.constraints.NotNull;
import one.theone.server.domain.event.entity.Event;

public record EventStatusUpdateRequest(
        @NotNull(message = "이벤트 상태는 필수입니다")
        Event.EventStatus status
) {
}
