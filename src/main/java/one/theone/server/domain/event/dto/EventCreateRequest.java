package one.theone.server.domain.event.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import one.theone.server.domain.event.entity.Event;

import java.time.LocalDateTime;

public record EventCreateRequest(
        @NotBlank(message = "이벤트명은 필수입니다")
        String name,

        @NotNull(message = "이벤트 타입은 필수입니다")
        Event.EventType type,

        @NotNull(message = "이벤트 시작일은 필수입니다")
        @FutureOrPresent(message = "이벤트 시작일은 현재 이후여야 합니다")
        LocalDateTime startAt,

        @NotNull(message = "이벤트 종료일은 필수입니다")
        @Future(message = "이벤트 종료일은 미래여야 합니다")
        LocalDateTime endAt,

        EventDetailRequest details,

        @NotNull(message = "이벤트 보상은 필수입니다")
        EventRewardRequest rewards
) {}
