package one.theone.server.domain.chat.repository;

import one.theone.server.domain.chat.entity.ChatMessage;
import one.theone.server.domain.chat.entity.QChatMessage;
import one.theone.server.domain.chat.entity.SenderType;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findMessages(Long roomId, Long lastMessageId, int size);
    long countUnread(Long roomId, Long lastMessageId, SenderType senderType);
}
