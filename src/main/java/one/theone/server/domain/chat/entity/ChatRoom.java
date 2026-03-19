package one.theone.server.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_rooms")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Member customer; // 문의 생성 회원

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Member manager; // 담당 관리자 (NULL 가능)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatStatus status = ChatStatus.WAITING;

    @Column(nullable = false)
    private LocalDateTime lastMessageAt = LocalDateTime.now(); // 목록 최신순 정렬용

    private LocalDateTime closedAt; // 상담 종료 시각

    public static ChatRoom create(String name, Member customer) {
        ChatRoom room = new ChatRoom();
        room.name = name;
        room.customer = customer;
        return room;
    }

    public void assignManager(Member manager) {
        this.manager = manager;
        this.status = ChatStatus.IN_PROGRESS;
    }

    public void updateLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }
}
