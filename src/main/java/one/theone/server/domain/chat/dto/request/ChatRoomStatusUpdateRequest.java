package one.theone.server.domain.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import one.theone.server.domain.chat.entity.ChatRoomStatus;

public record ChatRoomStatusUpdateRequest(
        @NotNull(message = "상태값은 필수입니다")
        ChatRoomStatus status
) {
}
