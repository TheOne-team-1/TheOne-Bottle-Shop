package one.theone.server.domain.point.repository;

import one.theone.server.domain.point.entity.PointUseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointUseDetailRepository extends JpaRepository<PointUseDetail, Long> {

    List<PointUseDetail> findByOrderId(Long orderId);
}
