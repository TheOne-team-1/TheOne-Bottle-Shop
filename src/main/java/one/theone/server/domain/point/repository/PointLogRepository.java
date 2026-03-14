package one.theone.server.domain.point.repository;

import one.theone.server.domain.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointLogRepository extends JpaRepository<PointLog, Long>, PointLogQueryRepository {

    @Query("SELECT COALESCE(SUM(pl.amount), 0) FROM PointLog pl WHERE pl.memberId = :memberId")
    Long sumAmountByMemberId(@Param("memberId") Long memberId);
}
