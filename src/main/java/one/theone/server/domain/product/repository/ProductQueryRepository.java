package one.theone.server.domain.product.repository;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.ProductsGetRequest;
import one.theone.server.domain.product.dto.ProductsGetResponse;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductQueryRepository {

    PageResponse<ProductSearchResponse> findProductByKeyword(String keyword, Pageable pageable);

    Page<ProductsGetResponse> findProductWithConditions(Pageable pageable, ProductsGetRequest request);
}
