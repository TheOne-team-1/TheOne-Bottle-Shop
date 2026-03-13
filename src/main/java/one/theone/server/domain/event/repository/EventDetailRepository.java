package one.theone.server.domain.event.repository;

import one.theone.server.domain.event.entity.EventDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDetailRepository extends JpaRepository<EventDetail, Long> {
}
