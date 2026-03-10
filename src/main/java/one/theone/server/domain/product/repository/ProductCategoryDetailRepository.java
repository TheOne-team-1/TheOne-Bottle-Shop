package one.theone.server.domain.product.repository;

import one.theone.server.domain.product.entity.ProductCategoryDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryDetailRepository extends JpaRepository<ProductCategoryDetail, Long> {
}
