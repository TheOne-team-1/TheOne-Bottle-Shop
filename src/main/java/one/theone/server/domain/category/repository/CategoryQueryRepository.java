package one.theone.server.domain.category.repository;

import one.theone.server.domain.category.dto.CategoriesGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryQueryRepository {

    Page<CategoriesGetResponse> findAllCategories(Pageable pageable);
}
