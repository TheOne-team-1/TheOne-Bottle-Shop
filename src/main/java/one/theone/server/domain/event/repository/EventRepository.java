package one.theone.server.domain.event.repository;

import one.theone.server.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long>, EventQueryRepository {
}
