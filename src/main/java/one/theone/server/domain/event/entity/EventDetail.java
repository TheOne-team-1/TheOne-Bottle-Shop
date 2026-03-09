package one.theone.server.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

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
}
