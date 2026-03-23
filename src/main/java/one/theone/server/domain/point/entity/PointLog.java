package one.theone.server.domain.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.domain.point.dto.PointAdjustRequest;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "point_logs", indexes = {
        @Index(name = "idx_point_log_member_id_created_at", columnList = "member_id, created_at")
})
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

    private String description;

    private Long amount;

    private Long remainingAmount;

    private Long balanceSnap;

    private LocalDate expiresAt;

    public enum PointType {
        EARN, USE, REFUND, EXPIRED, ADMIN
    }

    public static PointLog ofAdmin(Long memberId, PointAdjustRequest request, Long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.type = PointType.ADMIN;
        pointLog.amount = request.amount();
        pointLog.description = request.description();
        pointLog.balanceSnap = balanceSnap;

        if (request.amount() > 0) {
            pointLog.remainingAmount = request.amount();
            pointLog.expiresAt = LocalDate.now().plusYears(1);
        }

        return pointLog;
    }

    public static PointLog ofUse(Long memberId, Long orderId, String orderNum, Long amount, Long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.orderId = orderId;
        pointLog.type = PointType.USE;
        pointLog.description = orderNum + " 사용";
        pointLog.amount = amount;
        pointLog.balanceSnap = balanceSnap;

        return pointLog;
    }

    public static PointLog ofRefund(Long memberId, Long orderId, String orderNum, Long amount, Long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.orderId = orderId;
        pointLog.type = PointType.REFUND;
        pointLog.description = orderNum + " 환불";
        pointLog.amount = amount;
        pointLog.balanceSnap = balanceSnap;

        return pointLog;
    }

    public static PointLog ofEarn(Long memberId, Long orderId, String orderNum, Long amount, Long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.orderId = orderId;
        pointLog.type = PointType.EARN;
        pointLog.description = orderNum + " 적립";
        pointLog.amount = amount;
        pointLog.remainingAmount = amount;
        pointLog.balanceSnap = balanceSnap;
        pointLog.expiresAt = LocalDate.now().plusYears(1);

        return pointLog;
    }

    public static PointLog ofExpired(Long memberId, Long amount, Long balanceSnap) {
        PointLog pointLog = new PointLog();

        pointLog.memberId = memberId;
        pointLog.type = PointType.EXPIRED;
        pointLog.description = "포인트 만료";
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
