package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CategoryExceptionEnum;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.category.entity.CategoryDetail;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.product.dto.*;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryDetailRepository categoryDetailRepository;
    private final ProductViewService productViewService;

    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request) {
        categoryDetailRepository.findById(request.productCategoryDetailId())
                .orElseThrow(() -> new ServiceErrorException(CategoryExceptionEnum.ERR_CATEGORY_NOT_FOUND));

        Product product = Product.register(
                request.name(),
                request.price(),
                request.abv(),
                request.volumeMl(),
                request.productCategoryDetailId(),
                request.quantity()
        );

        productRepository.save(product);
        return ProductCreateResponse.from(product);
    }

    @Cacheable(
            value = "productCache",
            key = "'list:' + #request.sortType + ':' + #request.categoryIds + ':' + #request.abvMin + ':' + #request.abvMax + ':' + " +
                    "#request.priceMin + ':' + #request.priceMax + ':' + #request.volumeMl + ':' + #pageable.pageNumber + ':' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    public PageResponse<ProductsGetResponse> getProducts(ProductsGetRequest request, Pageable pageable) {
        Page<ProductsGetResponse> page = productRepository.findProductWithConditions(pageable, request);
        return PageResponse.register(page);
    }

    @Transactional(readOnly = true)
    public ProductGetResponse getProduct(Long id, String clientIp) {
        ProductGetResponse response = productRepository.findProductById(id);
        if (response == null) {
            throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND);
        }

        productViewService.record(id, clientIp);

        return response.withViewCount(productViewService.getViewCount(id));
    }

    @Transactional
    public ProductUpdateResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        if (request.categoryDetailId() != null) {
            categoryDetailRepository.findById(request.categoryDetailId())
                    .orElseThrow(() -> new ServiceErrorException(CategoryExceptionEnum.ERR_CATEGORY_NOT_FOUND));
        }

        product.update(request);

        return ProductUpdateResponse.from(product);
    }

    @Transactional
    public ProductStatusUpdateResponse updateProductStatus(Long id, ProductStatusUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.updateStatus(request);

        return ProductStatusUpdateResponse.from(product);
    }
}
