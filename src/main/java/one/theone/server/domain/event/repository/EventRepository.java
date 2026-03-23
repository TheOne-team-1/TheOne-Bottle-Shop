package one.theone.server.domain.event.repository;

import one.theone.server.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, EventQueryRepository {
    List<Event> findByStatusAndDeletedFalse(Event.EventStatus status);
}
