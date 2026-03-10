package one.theone.server.domain.product.repository;

import one.theone.server.domain.product.dto.ProductsGetRequest;
import one.theone.server.domain.product.dto.ProductsGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCustomRepository {
    Page<ProductsGetResponse> findAllWithConditions(Pageable pageable, ProductsGetRequest request);
}
