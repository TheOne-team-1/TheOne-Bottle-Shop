package one.theone.server.domain.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "point_use_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointUseDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pointLogId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long amount;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime usedAt;

    public static PointUseDetail register(Long pointLogId, Long orderId, Long amount) {
        PointUseDetail pointUseDetail = new PointUseDetail();

        pointUseDetail.pointLogId = pointLogId;
        pointUseDetail.orderId = orderId;
        pointUseDetail.amount = amount;

        return pointUseDetail;
    }
}
