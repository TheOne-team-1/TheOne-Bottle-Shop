package one.theone.server.domain.category.repository;

import one.theone.server.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryQueryRepository {

    boolean existsByName(String name);

    boolean existsBySortNum(Integer sortNum);

    List<Category> findAllBySortNumGreaterThanEqual(Integer sortNum);
}
