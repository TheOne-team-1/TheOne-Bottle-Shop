package one.theone.server.order.repository;

import one.theone.server.order.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderDetail, Long> {
}
