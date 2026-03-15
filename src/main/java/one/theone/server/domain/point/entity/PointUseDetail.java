package one.theone.server.domain.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.PointExceptionEnum;
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

    private Boolean refunded = Boolean.FALSE;

    public static PointUseDetail register(Long pointLogId, Long orderId, Long amount) {
        PointUseDetail pointUseDetail = new PointUseDetail();

        pointUseDetail.pointLogId = pointLogId;
        pointUseDetail.orderId = orderId;
        pointUseDetail.amount = amount;

        return pointUseDetail;
    }

    public void markRefunded() {
        if(this.refunded) {
            throw new ServiceErrorException(PointExceptionEnum.ERR_POINT_ALREADY_REFUNDED);
        }
        this.refunded = true;
    }
}
