package one.theone.server.domain.event.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.event.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
}
