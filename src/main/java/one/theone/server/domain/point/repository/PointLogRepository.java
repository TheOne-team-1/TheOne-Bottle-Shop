package one.theone.server.domain.point.repository;

import one.theone.server.domain.point.entity.PointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog, Long>, PointLogQueryRepository {

    @Query("SELECT COALESCE(SUM(pl.amount), 0) FROM PointLog pl WHERE pl.memberId = :memberId")
    Long sumAmountByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT pl FROM PointLog pl WHERE pl.memberId = :memberId AND pl.type = 'EARN' AND " +
            "pl.remainingAmount > 0 AND pl.expiredAt > CURRENT_DATE ORDER BY pl.expiredAt ASC")
    List<PointLog> findAvailablePoints(@Param("memberId") Long memberId);
}
