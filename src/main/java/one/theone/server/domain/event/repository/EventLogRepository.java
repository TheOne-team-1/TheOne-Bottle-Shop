package one.theone.server.domain.event.repository;

import one.theone.server.domain.event.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    boolean existsByOrderIdAndEventRewardIdAndStatus(Long orderId, Long eventRewardId, EventLog.EventLogStatus status);
}
