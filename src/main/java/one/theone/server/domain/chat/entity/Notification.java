package one.theone.server.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.domain.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications", indexes = {
        @Index(name = "idx_receiver_id_is_read", columnList = "receiver_id, is_read")
})
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver; // 알림 받을 사람

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // NEW_PRODUCT, NEW_COMMENT

    private String relatedUrl; // 클릭 시 이동할 상세 주소

    @Column(nullable = false)
    private Boolean isRead = false;

    public static Notification create(Member receiver, String content, NotificationType type, String url) {
        Notification notification = new Notification();
        notification.receiver = receiver;
        notification.content = content;
        notification.type = type;
        notification.relatedUrl = url;
        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}