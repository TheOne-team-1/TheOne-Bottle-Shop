package one.theone.server.domain.chat.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ChatExceptionEnum;
import one.theone.server.domain.chat.dto.request.ChatMessageSendRequest;
import one.theone.server.domain.chat.dto.request.ChatRoomCreateRequest;
import one.theone.server.domain.chat.dto.response.ChatMessageResponse;
import one.theone.server.domain.chat.dto.response.ChatRoomResponse;
import one.theone.server.domain.chat.entity.ChatMessage;
import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.entity.ChatRoomStatus;
import one.theone.server.domain.chat.entity.SenderType;
import one.theone.server.domain.chat.repository.ChatMessageRepository;
import one.theone.server.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse createRoom(Long customerId, ChatRoomCreateRequest request) {
        ChatRoom room = ChatRoom.create(request.name(), customerId);
        ChatRoom saved = chatRoomRepository.save(room);
        return ChatRoomResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long customerId) {
        return chatRoomRepository.findMyRooms(customerId)
                .stream()
                .map(ChatRoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getAdminRooms(ChatRoomStatus status) {
        return chatRoomRepository.findAdminRooms(status)
                .stream()
                .map(ChatRoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(Long memberId, Long roomId) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateRoomAccess(memberId, room);
        return ChatRoomResponse.from(room);
    }

    @Transactional
    public ChatMessageResponse saveMessage(
            Long senderId, SenderType senderType, Long roomId, ChatMessageSendRequest request
    ) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateRoomAccess(senderId, room);

        ChatMessage message = ChatMessage.createText(roomId,senderId, senderType, request.content());
        ChatMessage saved = chatMessageRepository.save(message);

        room.updateLastMessageAt(saved.getCreatedAt());

        return ChatMessageResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long  memberId, Long roomId, Long lastMessageId) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateRoomAccess(memberId, room);

        return chatMessageRepository.findMessages(roomId, lastMessageId, 20)
                .stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    private ChatRoom getRoomOrThrow(Long roomId) {
        return chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_NOT_FOUND));
    }

    private void validateRoomAccess(Long memberId, ChatRoom room) {
        boolean isCustomer = room.getCustomerId().equals(memberId);
        boolean isManager = room.getManagerId() != null && room.getManagerId().equals(memberId);

        if (!isCustomer && !isManager) {
            throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_ACCESS_DENIED);
        }
    }
}
