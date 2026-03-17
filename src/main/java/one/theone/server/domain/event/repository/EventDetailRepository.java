package one.theone.server.domain.event.repository;

import one.theone.server.domain.event.entity.EventDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventDetailRepository extends JpaRepository<EventDetail, Long> {
    Optional<EventDetail> findByEventIdAndDeletedFalse(Long eventId);
}
