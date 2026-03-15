package one.theone.server.domain.freebieCategory.repository;

import one.theone.server.domain.freebieCategory.entity.FreebieCategoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreebieCategoryDetailRepository extends JpaRepository<FreebieCategoryDetail, Long> {

    boolean existsByFreebieCategoryIdAndDeletedFalse(Long freebieCategoryId);

    boolean existsByFreebieCategoryIdAndName(Long freebieCategoryId, String name);

    boolean existsByFreebieCategoryIdAndSortNum(Long freebieCategoryId, Integer sortNum);

    List<FreebieCategoryDetail> findAllByFreebieCategoryIdAndSortNumGreaterThanEqual(Long freebieCategoryId, Integer sortNum);
}
