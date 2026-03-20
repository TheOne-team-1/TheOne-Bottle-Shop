package one.theone.server.domain.point.repository;

import jakarta.persistence.LockModeType;
import one.theone.server.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Point p WHERE p.memberId = :memberId")
    Optional<Point> findByMemberIdWithLock(@Param("memberId") Long memberId);
}
