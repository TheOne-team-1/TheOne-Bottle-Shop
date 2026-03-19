package one.theone.server.domain.chat.repository;

import one.theone.server.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findMessages(Long roomId, Long lastMessageId, int size);
}
