package one.theone.server.domain.chat.repository;

import one.theone.server.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {
    Optional<ChatMessage> findByIdAndChatRoomId(Long id, Long chatRoomId);
}
