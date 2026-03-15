package one.theone.server.domain.freebieCategory.repository;

import one.theone.server.domain.freebieCategory.dto.response.FreebieCategoriesGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FreebieCategoryQueryRepository {

    Page<FreebieCategoriesGetResponse> findAllFreebieCategories(Pageable pageable);
}
