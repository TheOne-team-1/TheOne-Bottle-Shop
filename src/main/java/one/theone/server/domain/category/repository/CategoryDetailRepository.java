package one.theone.server.domain.category.repository;

import one.theone.server.domain.category.entity.CategoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryDetailRepository extends JpaRepository<CategoryDetail, Long> {

    boolean existsByCategoryIdAndName(Long categoryId, String name);

    boolean existsByCategoryIdAndSortNum(Long categoryId, Integer sortNum);

    List<CategoryDetail> findAllByCategoryIdAndSortNumGreaterThanEqual(Long categoryId, Integer sortNum);
}
