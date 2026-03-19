package one.theone.server.domain.chat.dto.response;

import one.theone.server.domain.chat.entity.ChatMessage;
import one.theone.server.domain.chat.entity.MessageType;
import one.theone.server.domain.chat.entity.SenderType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long messageId,
        Long roomId,
        Long senderId,
        SenderType senderType,
        MessageType messageType,
        String content,
        boolean deleted,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoomId(),
                message.getSenderId(),
                message.getSenderType(),
                message.getMessageType(),
                message.getContent(),
                message.isDeleted(),
                message.getCreatedAt()
        );
    }
}
