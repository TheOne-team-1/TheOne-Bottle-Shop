package one.theone.server.domain.chat.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.chat.dto.request.ChatRoomCreateRequest;
import one.theone.server.domain.chat.dto.response.ChatRoomResponse;
import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional
    public ChatRoomResponse createRoom(Long customerId, ChatRoomCreateRequest request) {
        ChatRoom room = ChatRoom.create(request.name(), customerId);
        ChatRoom saved = chatRoomRepository.save(room);
        return ChatRoomResponse.from(saved);
    }
}
