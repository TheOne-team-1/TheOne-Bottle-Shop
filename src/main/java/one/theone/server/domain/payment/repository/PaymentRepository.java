package one.theone.server.domain.payment.repository;

import one.theone.server.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatusAndPayAtBefore(Payment.PaymentStatus status, LocalDateTime threshold);
}
