package one.theone.server.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "event_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Long eventRewardId;

    @Column(nullable = false)
    private Long memberId;

    private Long orderId;

    private LocalDateTime eventAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventLogStatus status;

    public static EventLog registerComplete(Long eventId, Long eventRewardId, Long memberId, Long orderId) {
        EventLog eventLog = new EventLog();
        eventLog.eventId = eventId;
        eventLog.eventRewardId = eventRewardId;
        eventLog.memberId = memberId;
        eventLog.orderId = orderId;
        eventLog.eventAt = LocalDateTime.now();
        eventLog.status = EventLogStatus.COMPLETE;
        return eventLog;
    }

    public static EventLog registerFail(Long eventId, Long eventRewardId, Long memberId, Long orderId) {
        EventLog eventLog = new EventLog();
        eventLog.eventId = eventId;
        eventLog.eventRewardId = eventRewardId;
        eventLog.memberId = memberId;
        eventLog.orderId = orderId;
        eventLog.eventAt = LocalDateTime.now();
        eventLog.status = EventLogStatus.FAIL;
        return eventLog;
    }

    public enum EventLogStatus {
        COMPLETE, FAIL
    }
}
