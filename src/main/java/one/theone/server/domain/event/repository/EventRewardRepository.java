package one.theone.server.domain.event.repository;

import one.theone.server.domain.event.entity.EventReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRewardRepository extends JpaRepository<EventReward, Long> {
    Optional<EventReward> findByEventIdAndDeletedFalse(Long eventId);
    Optional<EventReward> findByFreebieIdAndDeletedFalse(Long eventId);
    Optional<EventReward> findByCouponIdAndDeletedFalse(Long eventId);
}
