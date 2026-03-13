package one.theone.server.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event_rewards")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventReward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventRewardType rewardType;

    private Long couponId;

    private Long freebieId;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deletedAt;

    public static EventReward couponReward(Long eventId, Long couponId) {
        EventReward reward = new EventReward();
        reward.eventId = eventId;
        reward.rewardType = EventRewardType.COUPON;
        reward.couponId = couponId;
        reward.deleted = false;
        return reward;
    }

    public static EventReward freebieReward(Long eventId, Long freebieId) {
        EventReward reward = new EventReward();
        reward.eventId = eventId;
        reward.rewardType = EventRewardType.FREEBIE;
        reward.freebieId = freebieId;
        reward.deleted = false;
        return reward;
    }

    public enum EventRewardType {
        COUPON, FREEBIE
    }
}
