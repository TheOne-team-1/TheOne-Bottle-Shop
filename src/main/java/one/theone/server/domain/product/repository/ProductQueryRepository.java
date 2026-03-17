package one.theone.server.domain.product.repository;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.*;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductQueryRepository {

    PageResponse<ProductSearchResponse> findProductByKeyword(List<String> keywordMorphemes, String keyword, Pageable pageable);

    Page<ProductsGetResponse> findProductWithConditions(Pageable pageable, ProductsGetRequest request);

    Page<AdminProductsGetResponse> findAdminProductWithConditions(Pageable pageable, AdminProductsGetRequest request);

    ProductGetResponse findProductById(Long id);
}
