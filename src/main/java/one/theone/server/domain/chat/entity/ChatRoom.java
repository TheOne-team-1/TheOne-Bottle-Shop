package one.theone.server.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "manager_id")
    private Long managerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ChatRoomStatus status;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @Column(name = "customer_last_read_message_id")
    private Long customerLastReadMessageId;

    @Column(name = "manager_last_read_mesasge_id")
    private Long managerLastReadMesasgeId;

    private ChatRoom(String name, Long customerId) {
        this.name = name;
        this.customerId = customerId;
        this.status = ChatRoomStatus.WAITING;
    }

    public static ChatRoom create(String name, Long customerId) {
        return new ChatRoom(name, customerId);
    }

    public void assignManager(long managerId) {
        this.managerId = managerId;
    }

    public void unassignManager() {
        this.managerId = null;
    }

    public void changeStatus(ChatRoomStatus status) {
        this.status = status;
        if (status == ChatRoomStatus.COMPLETED) {
            this.closedAt = LocalDateTime.now();
            return;
        }

        this.closedAt = null;
    }

    public void updateLastMessageAt(LocalDateTime time) {
        this.lastMessageAt = time;
    }

    public void updateLastMessage(Long messageId, LocalDateTime time) {
        this.lastMessageId = messageId;
        this.lastMessageAt = time;
    }

    public void markRead(Long memberId) {
        if (this.customerId.equals(memberId)) {
            this.customerLastReadMessageId = memberId;
            return;
        }

        if (this.managerId != null && this.managerId.equals(memberId)) {
            this.managerLastReadMesasgeId = memberId;
        }
    }
}
