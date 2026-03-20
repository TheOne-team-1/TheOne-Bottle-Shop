package one.theone.server.domain.event.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.EventExceptionEnum;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.event.dto.*;
import one.theone.server.domain.event.entity.Event;
import one.theone.server.domain.event.entity.EventDetail;
import one.theone.server.domain.event.entity.EventReward;
import one.theone.server.domain.event.repository.EventDetailRepository;
import one.theone.server.domain.event.repository.EventRepository;
import one.theone.server.domain.event.repository.EventRewardRepository;
import one.theone.server.domain.freebie.entity.Freebie;
import one.theone.server.domain.freebie.repository.FreebieRepository;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventDetailRepository eventDetailRepository;
    private final EventRewardRepository eventRewardRepository;
    private final FreebieRepository freebieRepository;
    private final CouponRepository couponRepository;

    @Transactional
    @CacheEvict(value = "eventListCache", allEntries = true, cacheManager = "redisCacheManager")
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

        EventReward eventReward = EventReward.registerByRewardType(
                event.getId(),
                request.rewards());
        eventRewardRepository.save(eventReward);

        return new EventCreateResponse(event.getId(), eventReward.getId());
    }

    @Transactional
    @CacheEvict(value = "eventListCache", allEntries = true, cacheManager = "redisCacheManager")
    public EventStatusUpdateResponse updateEventStatus(Long eventId, EventStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ServiceErrorException(EventExceptionEnum.ERR_EVENT_NOT_FOUND)
        );
        event.updateStatus(request.status());

        return new EventStatusUpdateResponse(event.getId(), event.getName());
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "eventListCache",
            key = "#isAdmin + ':' + #request.status() + ':' + #page + ':' + #size",
            condition = "#request.startAt() == null && #request.endAt() == null",
            cacheManager = "redisCacheManager"
    )
    public PageResponse<EventsGetResponse> getEvents(EventsGetRequest request, int page, int size, boolean isAdmin) {
        validateStatusAccess(request.status(), isAdmin);

        if (request.startAt() != null && request.endAt() != null && !request.endAt().isAfter(request.startAt())) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_END_BEFORE_START);
        }
        List<Event.EventStatus> statuses = cleanStatuses(request.status(), isAdmin);

        return eventRepository.findEventsWithConditions(request, PageRequest.of(page, size), statuses, isAdmin);
    }

    @Transactional(readOnly = true)
    public EventGetResponse getEvent(Long eventId, boolean isAdmin) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ServiceErrorException(EventExceptionEnum.ERR_EVENT_NOT_FOUND)
        );
        validateStatusAccess(event.getStatus(), isAdmin);

        return eventRepository.findEventInfoById(event.getId(), isAdmin);
    }

    @Transactional
    @CacheEvict(value = "eventListCache", allEntries = true, cacheManager = "redisCacheManager")
    public EventDeleteResponse deleteEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new ServiceErrorException(EventExceptionEnum.ERR_EVENT_NOT_FOUND)
        );
        event.delete();
        return new EventDeleteResponse(
                event.getId(),
                event.getName(),
                event.getDeleted(),
                event.getDeletedAt()
        );
    }

    // 적용 중인 이벤트 반환
    @Transactional(readOnly = true)
    public List<EventReward> getEventRewardIfExists(Order order, List<OrderDetail> orderDetails) {
        List<EventReward> eventRewardList = new ArrayList<>();

        // 현재 OPEN 상태인 이벤트 목록 조회
        List<Event> openEventList = eventRepository.findByStatusAndDeletedFalse(Event.EventStatus.OPEN);
        if (openEventList.isEmpty()) return eventRewardList;

        for (Event openEvent : openEventList) {
            // 이벤트 조건 충족 여부 확인
            if (!isEventProcessing(openEvent, order, orderDetails)) continue;

            // 조건 충족 시 보상 조회
            eventRewardRepository.findByEventIdAndDeletedFalse(openEvent.getId()).ifPresent(eventReward -> eventRewardList.add(eventReward));
        }

        return eventRewardList;
    }

    // 사은품 재고가 0이라면 이벤트 PAUSE 상태 변경
    @Transactional
    @CacheEvict(value = "eventListCache", allEntries = true, cacheManager = "redisCacheManager")
    public void pauseEventIfFreebieSoldOut(Long freebieId) {
        // 사은품 재고가 있으면 계속 상태 유지
        Freebie freebie = freebieRepository.findById(freebieId).orElse(null);
        if (freebie == null || freebie.getQuantity() > 0) return;

        // 이벤트 보상이 아니면 상관없이 진행
        EventReward reward = eventRewardRepository.findByFreebieIdAndDeletedFalse(freebieId).orElse(null);
        if (reward == null) return;

        // 이벤트 상태가 OPEN이 아니면 상관 없음
        Event event = eventRepository.findById(reward.getEventId()).orElse(null);
        if (event == null || event.getStatus() != Event.EventStatus.OPEN) return;

        // 이벤트 상태 PAUSE 변경
        event.updateStatus(Event.EventStatus.PAUSE);
    }

    // 쿠폰 가 0이라면 이벤트 PAUSE 상태 변경
    @Transactional
    @CacheEvict(value = "eventListCache", allEntries = true, cacheManager = "redisCacheManager")
    public void pauseEventIfCouponSoldOut(Long couponId) {
        // 쿠폰 재고가 있으면 계속 상태 유지
        Coupon coupon = couponRepository.findById(couponId).orElse(null);
        if (coupon == null || coupon.getIssuedQuantity() <= coupon.getAvailQuantity()) return;

        // 이벤트 보상이 아니면 상관없이 진행
        EventReward reward = eventRewardRepository.findByCouponIdAndDeletedFalse(couponId).orElse(null);
        if (reward == null) return;

        // 이벤트 상태가 OPEN이 아니면 상관 없음
        Event event = eventRepository.findById(reward.getEventId()).orElse(null);
        if (event == null || event.getStatus() != Event.EventStatus.OPEN) return;

        // 이벤트 상태 PAUSE 변경
        event.updateStatus(Event.EventStatus.PAUSE);
    }

    // 이벤트가 진행 가능한지 검증
    private boolean isEventProcessing(Event event, Order order, List<OrderDetail> orderDetails) {
        return switch (event.getType()) {
            case PRODUCT_BUY -> {
                // event_details 에서 지정된 상품 가져오기
                EventDetail eventDetail = eventDetailRepository.findByEventIdAndDeletedFalse(event.getId()).orElse(null);

                // event_detail 에 상품 조건이 없으면 없으면 false 반환
                if (eventDetail == null || eventDetail.getEventProductId() == null) yield false;

                // 주문 상품 중 이벤트 상품 조건에 일치하는게 있으면 반환
                yield orderDetails.stream().anyMatch(orderDetail -> orderDetail.getProductId().equals(eventDetail.getEventProductId()));
            }

            case AMOUNT_BUY -> {
                // 주문 총액이 최소 구매 금액 이상인지
                EventDetail detail = eventDetailRepository.findByEventIdAndDeletedFalse(event.getId()).orElse(null);

                // event_detail 에 가격 조건이 없으면 없으면 false 반환
                if (detail == null || detail.getMinPrice() == null) yield false;

                // 가격 조건 일치하는지 확인 후 반환
                yield order.getTotalAmount() >= detail.getMinPrice();
            }
        };
    }

    private void validateStatusAccess(Event.EventStatus status, boolean isAdmin) {
        if ((status == Event.EventStatus.PENDING || status == Event.EventStatus.PAUSE) && !isAdmin) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_ACCESS_DENIED);
        }
    }

    private List<Event.EventStatus> cleanStatuses(Event.EventStatus status, boolean isAdmin) {
        if (status != null) {
            return List.of(status);
        }
        return isAdmin ? List.of(Event.EventStatus.values()) : List.of(Event.EventStatus.OPEN, Event.EventStatus.CLOSE);
    }
}
