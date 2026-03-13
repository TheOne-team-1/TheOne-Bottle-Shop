package one.theone.server.domain.point.repository;

import one.theone.server.domain.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long>, PointLogQueryRepository {
}
