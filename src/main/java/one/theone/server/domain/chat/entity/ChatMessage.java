package one.theone.server.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.domain.member.entity.Member;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chatroom_id_id", columnList = "chat_room_id, id DESC")
})
@SQLDelete(sql = "UPDATE chat_messages SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType; // CUSTOMER, MANAGER, SYSTEM

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType; // TEXT, SYSTEM

    @Column(nullable = false)
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    public static ChatMessage create(Member sender, ChatRoom chatRoom, String content, SenderType senderType) {
        ChatMessage message = new ChatMessage();
        message.sender = sender;
        message.chatRoom = chatRoom;
        message.content = content;
        message.senderType = senderType;
        message.messageType = MessageType.TEXT;
        return message;
    }
}