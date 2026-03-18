package one.theone.server.domain.refund.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;

import java.time.LocalDateTime;

import static one.theone.server.common.exception.domain.RefundExceptionEnum.ERR_INVALID_PENDING;

@Getter
@Entity
@Table(name = "refunds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundReason reason;

    @Column(name = "reason_description")
    private String reasonDescription;

    @Column(name = "refund_at")
    private LocalDateTime refundAt;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum RefundStatus {
        PENDING, COMPLETED
    }

    public enum RefundReason {
        MEMBER_REQUEST, OUT_OF_STOCK, ADMIN_REQUEST, PROCESS_FAIL
    }

    public static Refund register(long orderId, long paymentId, long price, RefundReason reason, String reasonDescription) {
        Refund refund = new Refund();
        refund.orderId = orderId;
        refund.paymentId = paymentId;
        refund.price = price;
        refund.status = RefundStatus.PENDING;
        refund.reason = reason;
        refund.reasonDescription = reasonDescription;
        refund.deleted = false;
        return refund;
    }

    private void validatePending() {
        if(status != RefundStatus.PENDING) {
            throw new ServiceErrorException(ERR_INVALID_PENDING);
        }
    }

    public void updateComplete() {
        validatePending();
        this.status = RefundStatus.COMPLETED;
        this.refundAt = LocalDateTime.now();
    }
}
