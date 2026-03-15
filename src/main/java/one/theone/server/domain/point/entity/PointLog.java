package one.theone.server.domain.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "point_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private Long orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PointType type;

    private Long amount;

    private Long remainingAmount;

    private Long balanceSnap;

    private LocalDate expiredAt;

    public enum PointType {
        EARN, USE, REFUND, EXPIRED, ADMIN
    }

    public static PointLog ofAdmin(Long memberId, Long amount, Long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.type = PointType.ADMIN;
        pointLog.amount = amount;
        pointLog.balanceSnap = balanceSnap;

        if (amount > 0) {
            pointLog.remainingAmount = amount;
            pointLog.expiredAt = LocalDate.now().plusYears(1);
        }

        return pointLog;
    }

    public static PointLog ofUse(Long memberId, Long orderId, Long amount, Long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.orderId = orderId;
        pointLog.type = PointType.USE;
        pointLog.amount = amount;
        pointLog.balanceSnap = balanceSnap;

        return pointLog;
    }

    public static PointLog ofRefund(Long memberId, Long orderId, Long amount, long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.orderId = orderId;
        pointLog.type = PointType.REFUND;
        pointLog.amount = amount;
        pointLog.balanceSnap = balanceSnap;

        return pointLog;
    }

    public void deduct(Long amount) {
        this.remainingAmount -= amount;
    }

    public void restore(Long amount) {
        this.remainingAmount += amount;
    }
}
