package one.theone.server.domain.freebie.repository;

import one.theone.server.domain.freebie.entity.Freebie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreebieRepository extends JpaRepository<Freebie, Long>, FreebieQueryRepository {
}
