package one.theone.server.domain.refund.repository;

import one.theone.server.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    boolean existsByOrderIdAndStatusAndDeletedFalse(Long orderId, Refund.RefundStatus status);
}
