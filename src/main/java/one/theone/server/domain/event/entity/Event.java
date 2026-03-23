package one.theone.server.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.EventExceptionEnum;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deletedAt;

    public static Event register(String name, LocalDateTime startAt, LocalDateTime endAt, EventType type) {
        validateTime(startAt, endAt);

        Event event = new Event();
        event.name = name;
        event.startAt = startAt;
        event.endAt = endAt;
        event.type = type;
        event.status = EventStatus.PENDING;
        event.deleted = false;
        return event;
    }

    private static void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_END_BEFORE_START);
        }
    }

    public void updateStatus(EventStatus status) {
        validateStatus(status);
        this.status = status;
    }

    private void validateStatus(EventStatus status) {
        boolean valid = switch (this.status) {
            case PENDING, PAUSE -> status == EventStatus.OPEN || status == EventStatus.CLOSE;
            case OPEN -> status == EventStatus.PAUSE;
            case CLOSE -> false;
        };

        if (!valid) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_STATUS_INVALID);
        }
    }

    public void delete() {
        if (this.deleted) {
            throw new ServiceErrorException(EventExceptionEnum.ERR_EVENT_ALREADY_DELETED);
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public enum EventType {
        PRODUCT_BUY, AMOUNT_BUY
    }

    public enum EventStatus {
        PENDING, OPEN, PAUSE, CLOSE
    }
}
