package one.theone.server.domain.chat.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ChatExceptionEnum;
import one.theone.server.domain.chat.dto.request.ChatMessageSendRequest;
import one.theone.server.domain.chat.dto.request.ChatRoomCreateRequest;
import one.theone.server.domain.chat.dto.request.ChatRoomStatusUpdateRequest;
import one.theone.server.domain.chat.dto.response.ChatMessageResponse;
import one.theone.server.domain.chat.dto.response.ChatRoomResponse;
import one.theone.server.domain.chat.entity.*;
import one.theone.server.domain.chat.repository.ChatMessageRepository;
import one.theone.server.domain.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Long SYSTEM_SENDER_ID = 0L;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatRoomResponse createRoom(Long customerId, ChatRoomCreateRequest request) {
        ChatRoom room = ChatRoom.create(request.name(), customerId);
        ChatRoom saved = chatRoomRepository.save(room);

        saveSystemMessage(saved, "문의가 접수되었습니다");

        return ChatRoomResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyRooms(Long customerId) {
        return chatRoomRepository.findMyRooms(customerId)
                .stream()
                .map(room -> ChatRoomResponse.from(room, getUnreadCount(customerId, room)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getAdminRooms(Long memberId, ChatRoomStatus status) {
        return chatRoomRepository.findAdminRooms(status)
                .stream()
                .map(room -> ChatRoomResponse.from(room, getUnreadCount(memberId, room)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getRoom(Long memberId, Long roomId) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateRoomAccess(memberId, room);
        return ChatRoomResponse.from(room,  getUnreadCount(memberId, room));
    }

    @Transactional
    public ChatMessageResponse saveMessage(
            Long senderId, SenderType senderType, Long roomId, ChatMessageSendRequest request
    ) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateRoomAccess(senderId, room);

        if (room.getStatus() == ChatRoomStatus.COMPLETED) {
            throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_ALREADY_COMPLETED);
        }

        ChatMessage message = ChatMessage.createText(roomId,senderId, senderType, request.content());
        ChatMessage saved = chatMessageRepository.save(message);

        room.updateLastMessage(saved.getId(), saved.getCreatedAt());

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

    @Transactional
    public ChatRoomResponse updateStatus(Long memberId, Long roomId, ChatRoomStatusUpdateRequest request) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateManagerOnly(memberId, room);

        ChatRoomStatus before = room.getStatus();
        ChatRoomStatus after = request.status();

        if (before == after) {
            return ChatRoomResponse.from(room);
        }

        if (after == ChatRoomStatus.WAITING) {
            room.unassignManager();
            room.changeStatus(ChatRoomStatus.WAITING);
            saveSystemMessage(room, "상담이 대기 상태로 변경되었습니다");
            return ChatRoomResponse.from(room);
        }

        if (after == ChatRoomStatus.COMPLETED) {
            room.changeStatus(ChatRoomStatus.COMPLETED);
            saveSystemMessage(room, "상담이 종료되었습니다");
            return ChatRoomResponse.from(room);
        }

        throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_STATUS_INVALID);
    }

    @Transactional
    public ChatRoomResponse assignManager(Long memberId, Long roomId) {
        ChatRoom room = getRoomForUpdateOrThrow(roomId);

        if (room.getManagerId() != null && !room.getManagerId().equals(memberId)) {
            throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_ALREADY_ASSIGNED);
        }

        boolean firstAssigned = room.getManagerId() == null;

        room.assignManager(memberId);

        if (room.getStatus() == ChatRoomStatus.WAITING) {
            room.changeStatus(ChatRoomStatus.IN_PROGRESS);
        }

        if (firstAssigned) {
            saveSystemMessage(room, "상담사가 배정되었습니다");
            saveSystemMessage(room, "상담이 시작되었습니다");
        }

        return ChatRoomResponse.from(room);
    }

    @Transactional
    public void markAsRead(Long memberId, Long roomId) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateRoomAccess(memberId, room);

        room.markRead(memberId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long memberId, ChatRoom room) {
        Long lastReadId;

        if (room.getCustomerId().equals(memberId)) {
            lastReadId = room.getCustomerLastReadMessageId();
            return chatMessageRepository.countUnread(room.getId(), lastReadId, SenderType.MANAGER);
        }

        if (room.getManagerId() != null && room.getManagerId().equals(memberId)) {
            lastReadId = room.getManagerLastReadMesasgeId();
            return chatMessageRepository.countUnread(room.getId(), lastReadId, SenderType.CUSTOMER);
        }

        return 0;
    }

    @Transactional
    public void deleteMessage(Long memberId, Long roomId, Long messageId) {
        ChatRoom room = getRoomOrThrow(roomId);
        validateRoomAccess(memberId, room);

        ChatMessage message = chatMessageRepository.findByIdAndChatRoomId(messageId, roomId)
                .orElseThrow(() -> new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_MESSAGE_NOT_FOUND));

        if (message.getMessageType() == MessageType.SYSTEM) {
            throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_SYSTEM_MESSAGE_DELETE_NOT_ALLOWED);
        }

        if (!message.getSenderId().equals(memberId)) {
            throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_MESSAGE_DELETE_FORBIDDEN);
        }

        if (!message.isDeleted()) {
            message.delete();
        }
    }

    private void saveSystemMessage(ChatRoom room, String content) {
        ChatMessage systemMessage = ChatMessage.createSystem(room.getId(), SYSTEM_SENDER_ID, content);
        ChatMessage saved = chatMessageRepository.save(systemMessage);

        room.updateLastMessageAt(saved.getCreatedAt());
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

    private void validateManagerOnly(Long memberId, ChatRoom room) {
        if (room.getManagerId() == null || !room.getManagerId().equals(memberId)) {
            throw new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_ACCESS_DENIED);
        }
    }

    private ChatRoom getRoomForUpdateOrThrow(Long roomId) {
        return chatRoomRepository.findByIdForUpdate(roomId).orElseThrow(
                () -> new ServiceErrorException(ChatExceptionEnum.ERR_CHAT_ROOM_NOT_FOUND)
        );
    }
}
