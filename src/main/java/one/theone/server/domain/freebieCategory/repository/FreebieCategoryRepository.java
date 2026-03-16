package one.theone.server.domain.freebieCategory.repository;

import one.theone.server.domain.freebieCategory.entity.FreebieCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreebieCategoryRepository extends JpaRepository<FreebieCategory, Long>, FreebieCategoryQueryRepository {

    boolean existsByName(String name);

    boolean existsBySortNum(Integer sortNum);

    List<FreebieCategory> findAllBySortNumGreaterThanEqual(Integer sortNum);
}
