package one.theone.server.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.EventExceptionEnum;
import one.theone.server.domain.event.dto.EventDetailRequest;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    private Long minPrice;

    private Long eventProductId;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deletedAt;

    public static EventDetail registerByEventType(Long eventId, Event.EventType type, EventDetailRequest details) {
        return switch (type) {
            case PRODUCT_BUY -> {
                if (details == null || details.eventProductId() == null) {
                    throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_PRODUCT_REQUIRED);
                }
                yield productBuyEvent(eventId, details.eventProductId());
            }
            case AMOUNT_BUY -> {
                if (details == null || details.minPrice() == null) {
                    throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_MIN_PRICE_REQUIRED);
                }
                yield amountBuyEvent(eventId, details.minPrice());
            }
        };
    }

    private static EventDetail amountBuyEvent(Long eventId, Long minPrice) {
        EventDetail eventDetail = new EventDetail();
        eventDetail.eventId = eventId;
        eventDetail.minPrice = minPrice;
        eventDetail.deleted = false;
        return eventDetail;
    }

    private static EventDetail productBuyEvent(Long eventId, Long eventProductId) {
        EventDetail eventDetail = new EventDetail();
        eventDetail.eventId = eventId;
        eventDetail.eventProductId = eventProductId;
        eventDetail.deleted = false;
        return eventDetail;
    }

    public static void validateDetails(EventDetailRequest details) {
        if (details != null && details.eventProductId() != null && details.minPrice() != null) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_DETAIL_INVALID);
        }
    }
}
