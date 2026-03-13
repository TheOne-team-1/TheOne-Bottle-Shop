package one.theone.server.domain.event.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.event.dto.EventCreateRequest;
import one.theone.server.domain.event.dto.EventCreateResponse;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventDetail;
import one.theone.server.domain.event.repository.EventDetailRepository;
import one.theone.server.domain.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventDetailRepository eventDetailRepository;

    @Transactional
    public EventCreateResponse createEvent(EventCreateRequest request) {
        EventDetail.validateDetails(request.details());

        Event event = Event.register(
                request.name(),
                request.startAt(),
                request.endAt(),
                request.type());
        eventRepository.save(event);

        EventDetail eventDetail = EventDetail.registerByEventType(
                event.getId(),
                event.getType(),
                request.details());
        eventDetailRepository.save(eventDetail);

        // 이벤트 보상 생성 & 저장

        // EventCreateResponse 반환
    }
}
