package one.theone.server.domain.category.repository;

import one.theone.server.domain.category.entity.CategoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryDetailRepository extends JpaRepository<CategoryDetail, Long> {
}
