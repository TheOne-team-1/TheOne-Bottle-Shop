package one.theone.server.domain.order.repository;

import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNum(String orderNum);
    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);
    Page<Order> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
