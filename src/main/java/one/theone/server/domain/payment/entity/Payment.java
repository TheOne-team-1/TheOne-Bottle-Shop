package one.theone.server.domain.payment.entity;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.PaymentExceptionEnum;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments", uniqueConstraints = { @UniqueConstraint(name = "uk_payment_unique_id", columnNames = "payment_unique_id")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(name = "payment_unique_id", nullable = false)
    private String paymentUniqueId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Long price_snap;

    private LocalDateTime pay_at;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deleted_at;

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    public static Payment register(Long orderId, Long price_snap) {
        Payment payment = new Payment();
        payment.orderId = orderId;
        payment.status = PaymentStatus.PENDING;
        payment.paymentUniqueId = String.format(
                "%s-%s"
                , "PAY"
                , UlidCreator.getUlid().toString() // ULID 적용
        );
        payment.deleted = false;
        payment.price_snap = price_snap;
        return payment;
    }

    //region 상태 검증
    private void validatePending() {
        if(this.status != PaymentStatus.PENDING) {
            throw new ServiceErrorException(PaymentExceptionEnum.ERR_INVALID_PENDING);
        }
    }

    private void validateComplete() {
        if(this.status != PaymentStatus.COMPLETED) {
            throw new ServiceErrorException(PaymentExceptionEnum.ERR_INVALID_COMPLETE);
        }
    }
    //endregion

    //region 상태 업데이트
    public void updateComplete() {
        validatePending();
        this.status = PaymentStatus.COMPLETED;
        this.pay_at = LocalDateTime.now();
    }

    public void updateFail() {
        validatePending();
        this.status = PaymentStatus.FAILED;
    }

    public void updateRefund() {
        validateComplete();
        this.status = PaymentStatus.REFUNDED;
    }
    //endregion
}
