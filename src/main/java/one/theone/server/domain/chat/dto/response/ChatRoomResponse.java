package one.theone.server.domain.chat.dto.response;

import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.entity.ChatRoomStatus;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long roomId,
        String name,
        Long customerId,
        Long managerId,
        ChatRoomStatus status,
        LocalDateTime lastMessageAt,
        LocalDateTime closedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ChatRoomResponse from(ChatRoom room) {
        return new ChatRoomResponse(
                room.getId(),
                room.getName(),
                room.getCustomerId(),
                room.getManagerId(),
                room.getStatus(),
                room.getLastMessageAt(),
                room.getClosedAt(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}
