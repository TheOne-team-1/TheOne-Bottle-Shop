package one.theone.server.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

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

    public enum EventRewardType {
        COUPON, FREEBIE
    }
}
