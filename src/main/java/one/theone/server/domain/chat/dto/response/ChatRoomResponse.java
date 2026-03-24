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
        LocalDateTime updatedAt,
        long unreadCount
) {
    public static ChatRoomResponse from(ChatRoom room, long unreadCount) {
        return new ChatRoomResponse(
                room.getId(),
                room.getName(),
                room.getCustomerId(),
                room.getManagerId(),
                room.getStatus(),
                room.getLastMessageAt(),
                room.getClosedAt(),
                room.getCreatedAt(),
                room.getUpdatedAt(),
                unreadCount
        );
    }

    public static ChatRoomResponse from(ChatRoom room) {
        return from(room, 0L);
    }
}
