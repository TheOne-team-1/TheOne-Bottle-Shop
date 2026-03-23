package one.theone.server.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private SenderType senderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

   private ChatMessage(
           Long chatRoomId,
           Long senderId,
           SenderType senderType,
           MessageType messageType,
           String content
   ) {
       this.chatRoomId = chatRoomId;
       this.senderId = senderId;
       this.senderType = senderType;
       this.messageType = messageType;
       this.content = content;
       this.deleted = false;
   }

   public static ChatMessage createText(
           Long chatRoomId,
           Long senderId,
           SenderType senderType,
           String content
   ) {
       return new  ChatMessage(chatRoomId, senderId, senderType, MessageType.TEXT, content);
   }

   public static ChatMessage createSystem(
           Long chatRoomId,
           Long senderId,
           String content
   ) {
       return new ChatMessage(chatRoomId, senderId, SenderType.SYSTEM, MessageType.SYSTEM, content);
   }

   public void delete() {
       this.deleted = true;
       this.content = "삭제된 메시지입니다";
   }
}
