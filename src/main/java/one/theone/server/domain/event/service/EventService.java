package one.theone.server.domain.event.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.event.dto.EventCreateRequest;
import one.theone.server.domain.event.dto.EventCreateResponse;
import one.theone.server.domain.event.dto.EventStatusUpdateRequest;
import one.theone.server.domain.event.dto.EventStatusUpdateResponse;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventDetail;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.repository.EventDetailRepository;
import one.theone.server.domain.event.repository.EventRepository;
import one.theone.server.domain.event.repository.EventRewardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventDetailRepository eventDetailRepository;
    private final EventRewardRepository eventRewardRepository;

    @Transactional
    public EventCreateResponse createEvent(EventCreateRequest request) {
        EventDetail.validateDetails(request.details());
        // TODO 사은품, 쿠폰 존재 여부 검증,, 사은품 재고, 쿠폰 만료일 검증

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

        EventReward eventReward = EventReward.registerByRewardType(
                event.getId(),
                request.rewards());
        eventRewardRepository.save(eventReward);

        return new EventCreateResponse(event.getId(), eventReward.getId());
    }

    @Transactional
    public EventStatusUpdateResponse updateEventStatus(EventStatusUpdateRequest request) {
    }
}
