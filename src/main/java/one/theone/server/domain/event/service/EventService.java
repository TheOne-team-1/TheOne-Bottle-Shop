package one.theone.server.domain.event.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.EventExceptionEnum;
import one.theone.server.domain.event.dto.*;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventDetail;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.repository.EventDetailRepository;
import one.theone.server.domain.event.repository.EventRepository;
import one.theone.server.domain.event.repository.EventRewardRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public EventStatusUpdateResponse updateEventStatus(Long eventId, EventStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ServiceErrorException(EventExceptionEnum.ERR_EVENT_NOT_FOUND)
        );
        event.updateStatus(request.status());

        return new EventStatusUpdateResponse(event.getId(), event.getName());
    }

    @Transactional(readOnly = true)
    public PageResponse<EventsGetResponse> getEvents(EventsGetRequest request, Pageable pageable, Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);

        if ((request.status() == Event.EventStatus.PENDING || request.status() == Event.EventStatus.PAUSE) && !isAdmin) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_ACCESS_DENIED);
        }
        if (request.startAt() != null && request.endAt() != null && !request.endAt().isAfter(request.startAt())) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_END_BEFORE_START);
        }
        List<Event.EventStatus> statuses = cleanStatuses(request.status(), isAdmin);

        return eventRepository.findEventsWithConditions(request, pageable, statuses, isAdmin);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private List<Event.EventStatus> cleanStatuses(Event.EventStatus status, boolean isAdmin) {
        if (status != null) {
            return List.of(status);
        }
        return isAdmin ? List.of(Event.EventStatus.values()) : List.of(Event.EventStatus.OPEN, Event.EventStatus.CLOSE);
    }
}
