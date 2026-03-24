package one.theone.server.domain.chat.service;

import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.chat.dto.request.ChatMessageSendRequest;
import one.theone.server.domain.chat.dto.request.ChatRoomCreateRequest;
import one.theone.server.domain.chat.dto.request.ChatRoomStatusUpdateRequest;
import one.theone.server.domain.chat.dto.response.ChatMessageResponse;
import one.theone.server.domain.chat.dto.response.ChatRoomResponse;
import one.theone.server.domain.chat.entity.ChatMessage;
import one.theone.server.domain.chat.entity.ChatRoom;
import one.theone.server.domain.chat.entity.ChatRoomStatus;
import one.theone.server.domain.chat.entity.MessageType;
import one.theone.server.domain.chat.entity.SenderType;
import one.theone.server.domain.chat.repository.ChatMessageRepository;
import one.theone.server.domain.chat.repository.ChatRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ChatService chatService;

    private static final Long CUSTOMER_ID = 1L;
    private static final Long MANAGER_ID = 99L;
    private static final Long OTHER_CUSTOMER_ID = 2L;
    private static final Long OTHER_MANAGER_ID = 100L;

    private ChatRoom createRoomEntity(Long roomId, Long customerId, Long managerId, ChatRoomStatus status, String name) {
        ChatRoom room = ChatRoom.create(name, customerId);
        ReflectionTestUtils.setField(room, "id", roomId);
        ReflectionTestUtils.setField(room, "managerId", managerId);
        ReflectionTestUtils.setField(room, "status", status);
        return room;
    }

    private ChatMessage createTextMessage(Long id, Long roomId, Long senderId, SenderType senderType, String content) {
        ChatMessage message = ChatMessage.createText(roomId, senderId, senderType, content);
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.now());
        return message;
    }

    private ChatMessage createSystemMessage(Long id, Long roomId, String content) {
        ChatMessage message = ChatMessage.createSystem(roomId, 0L, content);
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.now());
        return message;
    }

    @Nested
    class CreateRoomTest {

        @Test
        @DisplayName("채팅방 생성 성공")
        void createRoom_success() {
            ChatRoom room = ChatRoom.create("배송 문의", CUSTOMER_ID);
            ReflectionTestUtils.setField(room, "id", 1L);

            ChatMessage systemMessage = createSystemMessage(10L, 1L, "문의가 접수되었습니다");

            given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(room);
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(systemMessage);

            ChatRoomResponse response = chatService.createRoom(CUSTOMER_ID, new ChatRoomCreateRequest("배송 문의"));

            assertThat(response.roomId()).isEqualTo(1L);
            assertThat(response.customerId()).isEqualTo(CUSTOMER_ID);
            assertThat(response.status()).isEqualTo(ChatRoomStatus.WAITING);

            then(chatRoomRepository).should().save(any(ChatRoom.class));
            then(chatMessageRepository).should().save(any(ChatMessage.class));
        }
    }

    @Nested
    class SaveMessageTest {

        @Test
        @DisplayName("고객 메시지 전송 성공")
        void saveMessage_success_customer() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ChatMessage savedMessage = createTextMessage(100L, 1L, CUSTOMER_ID, SenderType.CUSTOMER, "안녕하세요");

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment("chat:unread:1:99")).willReturn(1L);
            given(redisTemplate.expire("chat:unread:1:99", Duration.ofMinutes(30))).willReturn(true);

            ChatMessageResponse response = chatService.saveMessage(
                    CUSTOMER_ID,
                    SenderType.CUSTOMER,
                    1L,
                    new ChatMessageSendRequest("안녕하세요")
            );

            assertThat(response.roomId()).isEqualTo(1L);
            assertThat(response.senderId()).isEqualTo(CUSTOMER_ID);
            assertThat(response.senderType()).isEqualTo(SenderType.CUSTOMER);
            assertThat(response.content()).isEqualTo("안녕하세요");

            then(valueOperations).should().increment("chat:unread:1:99");
        }

        @Test
        @DisplayName("관리자 메시지 전송 성공")
        void saveMessage_success_manager() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ChatMessage savedMessage = createTextMessage(101L, 1L, MANAGER_ID, SenderType.MANAGER, "관리자 메시지");

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(savedMessage);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment("chat:unread:1:1")).willReturn(1L);
            given(redisTemplate.expire("chat:unread:1:1", Duration.ofMinutes(30))).willReturn(true);

            ChatMessageResponse response = chatService.saveMessage(
                    MANAGER_ID,
                    SenderType.MANAGER,
                    1L,
                    new ChatMessageSendRequest("관리자 메시지")
            );

            assertThat(response.roomId()).isEqualTo(1L);
            assertThat(response.senderId()).isEqualTo(MANAGER_ID);
            assertThat(response.senderType()).isEqualTo(SenderType.MANAGER);
            assertThat(response.content()).isEqualTo("관리자 메시지");

            then(valueOperations).should().increment("chat:unread:1:1");
        }

        @Test
        @DisplayName("종료된 채팅방에는 메시지를 전송할 수 없다")
        void saveMessage_fail_completedRoom() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.COMPLETED, "문의");
            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));

            assertThatThrownBy(() -> chatService.saveMessage(
                    CUSTOMER_ID,
                    SenderType.CUSTOMER,
                    1L,
                    new ChatMessageSendRequest("종료 후 메시지")
            )).isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("참여자가 아닌 사용자는 메시지를 전송할 수 없다")
        void saveMessage_fail_accessDenied() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));

            assertThatThrownBy(() -> chatService.saveMessage(
                    OTHER_CUSTOMER_ID,
                    SenderType.CUSTOMER,
                    1L,
                    new ChatMessageSendRequest("침입")
            )).isInstanceOf(ServiceErrorException.class);
        }
    }

    @Nested
    class AssignManagerTest {

        @Test
        @DisplayName("관리자 배정 성공")
        void assignManager_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, null, ChatRoomStatus.WAITING, "문의");
            ChatMessage assignedMessage = createSystemMessage(11L, 1L, "상담사가 배정되었습니다");
            ChatMessage startedMessage = createSystemMessage(12L, 1L, "상담이 시작되었습니다");

            given(chatRoomRepository.findByIdForUpdate(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.save(any(ChatMessage.class)))
                    .willReturn(assignedMessage)
                    .willReturn(startedMessage);

            ChatRoomResponse response = chatService.assignManager(MANAGER_ID, 1L);

            assertThat(response.roomId()).isEqualTo(1L);
            assertThat(room.getManagerId()).isEqualTo(MANAGER_ID);
            assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.IN_PROGRESS);
            then(chatMessageRepository).should(times(2)).save(any(ChatMessage.class));
        }

        @Test
        @DisplayName("이미 다른 관리자가 배정된 채팅방에는 배정할 수 없다")
        void assignManager_fail_alreadyAssigned() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            given(chatRoomRepository.findByIdForUpdate(1L)).willReturn(Optional.of(room));

            assertThatThrownBy(() -> chatService.assignManager(OTHER_MANAGER_ID, 1L))
                    .isInstanceOf(ServiceErrorException.class);
        }
    }

    @Nested
    class UpdateStatusTest {

        @Test
        @DisplayName("WAITING으로 변경하면 관리자 배정이 해제된다")
        void updateStatus_waiting_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ChatMessage waitingMessage = createSystemMessage(13L, 1L, "상담이 대기 상태로 변경되었습니다");

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(waitingMessage);

            chatService.updateStatus(MANAGER_ID, 1L, new ChatRoomStatusUpdateRequest(ChatRoomStatus.WAITING));

            assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.WAITING);
            assertThat(room.getManagerId()).isNull();
            then(redisTemplate).should().delete("chat:unread:1:99");
        }

        @Test
        @DisplayName("COMPLETED로 변경하면 종료 메시지가 생성된다")
        void updateStatus_completed_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ChatMessage completedMessage = createSystemMessage(14L, 1L, "상담이 종료되었습니다");

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(completedMessage);

            chatService.updateStatus(MANAGER_ID, 1L, new ChatRoomStatusUpdateRequest(ChatRoomStatus.COMPLETED));

            assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.COMPLETED);
            assertThat(room.getClosedAt()).isNotNull();
        }
    }

    @Nested
    class ReadTest {

        @Test
        @DisplayName("고객이 읽음처리하면 customerLastReadMessageId가 갱신된다")
        void markAsRead_customer_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ReflectionTestUtils.setField(room, "lastMessageId", 101L);

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));

            chatService.markAsRead(CUSTOMER_ID, 1L);

            assertThat(room.getCustomerLastReadMessageId()).isEqualTo(101L);
            then(redisTemplate).should().delete("chat:unread:1:1");
        }

        @Test
        @DisplayName("관리자가 읽음처리하면 managerLastReadMessageId가 갱신된다")
        void markAsRead_manager_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ReflectionTestUtils.setField(room, "lastMessageId", 202L);

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));

            chatService.markAsRead(MANAGER_ID, 1L);

            assertThat(room.getManagerLastReadMessageId()).isEqualTo(202L);
            then(redisTemplate).should().delete("chat:unread:1:99");
        }
    }

    @Nested
    class UnreadCountTest {

        @Test
        @DisplayName("고객 기준 unreadCount 계산 성공 - Redis miss")
        void getUnreadCount_customer_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ReflectionTestUtils.setField(room, "customerLastReadMessageId", 10L);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("chat:unread:1:1")).willReturn(null);
            given(chatMessageRepository.countUnread(1L, 10L, SenderType.MANAGER)).willReturn(2L);

            long unreadCount = chatService.getUnreadCount(CUSTOMER_ID, room);

            assertThat(unreadCount).isEqualTo(2L);
            then(valueOperations).should().set("chat:unread:1:1", 2L, Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("관리자 기준 unreadCount 계산 성공 - Redis miss")
        void getUnreadCount_manager_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ReflectionTestUtils.setField(room, "managerLastReadMessageId", 20L);

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("chat:unread:1:99")).willReturn(null);
            given(chatMessageRepository.countUnread(1L, 20L, SenderType.CUSTOMER)).willReturn(3L);

            long unreadCount = chatService.getUnreadCount(MANAGER_ID, room);

            assertThat(unreadCount).isEqualTo(3L);
            then(valueOperations).should().set("chat:unread:1:99", 3L, Duration.ofMinutes(30));
        }

        @Test
        @DisplayName("고객 기준 unreadCount 계산 성공 - Redis hit")
        void getUnreadCount_customer_cacheHit_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("chat:unread:1:1")).willReturn(5L);

            long unreadCount = chatService.getUnreadCount(CUSTOMER_ID, room);

            assertThat(unreadCount).isEqualTo(5L);
            then(chatMessageRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("관리자 기준 unreadCount 계산 성공 - Redis hit")
        void getUnreadCount_manager_cacheHit_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("chat:unread:1:99")).willReturn(4L);

            long unreadCount = chatService.getUnreadCount(MANAGER_ID, room);

            assertThat(unreadCount).isEqualTo(4L);
            then(chatMessageRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    class DeleteMessageTest {

        @Test
        @DisplayName("본인 메시지는 soft delete 할 수 있다")
        void deleteMessage_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ChatMessage message = createTextMessage(101L, 1L, CUSTOMER_ID, SenderType.CUSTOMER, "삭제할 메시지");

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.findByIdAndChatRoomId(101L, 1L)).willReturn(Optional.of(message));

            chatService.deleteMessage(CUSTOMER_ID, 1L, 101L);

            assertThat(message.isDeleted()).isTrue();
            assertThat(message.getContent()).isEqualTo("삭제된 메시지입니다");
        }

        @Test
        @DisplayName("다른 사람 메시지는 삭제할 수 없다")
        void deleteMessage_fail_forbidden() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ChatMessage message = createTextMessage(101L, 1L, CUSTOMER_ID, SenderType.CUSTOMER, "고객 메시지");

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.findByIdAndChatRoomId(101L, 1L)).willReturn(Optional.of(message));

            assertThatThrownBy(() -> chatService.deleteMessage(MANAGER_ID, 1L, 101L))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("시스템 메시지는 삭제할 수 없다")
        void deleteMessage_fail_systemMessage() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            ChatMessage message = createSystemMessage(201L, 1L, "시스템 메시지");

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.findByIdAndChatRoomId(201L, 1L)).willReturn(Optional.of(message));

            assertThatThrownBy(() -> chatService.deleteMessage(CUSTOMER_ID, 1L, 201L))
                    .isInstanceOf(ServiceErrorException.class);
        }
    }

    @Nested
    class GetMessagesTest {

        @Test
        @DisplayName("메시지 목록 조회 성공")
        void getMessages_success() {
            ChatRoom room = createRoomEntity(1L, CUSTOMER_ID, MANAGER_ID, ChatRoomStatus.IN_PROGRESS, "문의");
            List<ChatMessage> messages = List.of(
                    createTextMessage(101L, 1L, CUSTOMER_ID, SenderType.CUSTOMER, "첫 번째"),
                    createTextMessage(102L, 1L, MANAGER_ID, SenderType.MANAGER, "두 번째")
            );

            given(chatRoomRepository.findById(1L)).willReturn(Optional.of(room));
            given(chatMessageRepository.findMessages(1L, null, 20)).willReturn(messages);

            List<ChatMessageResponse> responses = chatService.getMessages(CUSTOMER_ID, 1L, null);

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).content()).isEqualTo("첫 번째");
            assertThat(responses.get(1).content()).isEqualTo("두 번째");
        }
    }
}