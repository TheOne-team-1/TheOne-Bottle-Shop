package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.annotation.RedissonLock;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CategoryExceptionEnum;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.product.dto.*;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.review.dto.ReviewResponse;
import one.theone.server.domain.review.repository.ReviewRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static one.theone.server.common.config.cache.CacheConfig.PRODUCT_SEARCH;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryDetailRepository categoryDetailRepository;
    private final ProductViewService productViewService;
    private final ReviewRepository reviewRepository;

    // 관리자 전용 -----------------------------------------------------------------------------------------
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

    @Transactional(readOnly = true)
    public PageResponse<AdminProductsGetResponse> getAdminProducts(AdminProductsGetRequest request, int page, int size) {
        Page<AdminProductsGetResponse> adminProducts = productRepository.findAdminProductWithConditions(PageRequest.of(page, size), request);
        return PageResponse.register(adminProducts);
    }

    @Caching(evict = {
            @CacheEvict(value = "productCache", allEntries = true),
            @CacheEvict(value = PRODUCT_SEARCH, allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = "productCache", allEntries = true),
            @CacheEvict(value = PRODUCT_SEARCH, allEntries = true)
    })
    @Transactional
    public ProductStatusUpdateResponse updateProductStatus(Long id, ProductStatusUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.updateStatus(request);
        return ProductStatusUpdateResponse.from(product);
    }

    @Caching(evict = {
            @CacheEvict(value = "productCache", allEntries = true),
            @CacheEvict(value = PRODUCT_SEARCH, allEntries = true)
    })
    @Transactional
    public ProductDeleteResponse deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.delete();
        return ProductDeleteResponse.from(product);
    }


    // 일반 사용자 -----------------------------------------------------------------------------------------
    @Cacheable(
            value = "productCache",
            key = "'list:' + #request.sortType + ':' + #request.categoryIds + ':' + #request.abvMin + ':' + #request.abvMax + ':' + " +
                    "#request.priceMin + ':' + #request.priceMax + ':' + #request.volumeMl + ':' + #page + ':' + #size"
    )
    @Transactional(readOnly = true)
    public PageResponse<ProductsGetResponse> getProducts(ProductsGetRequest request, int page, int size) {
        Page<ProductsGetResponse> products = productRepository.findProductWithConditions(PageRequest.of(page, size), request);
        return PageResponse.register(products);
    }

    @Transactional(readOnly = true)
    public ProductGetResponse getProduct(Long id, String clientIp) {
        ProductGetResponse response = productRepository.findProductById(id);
        if (response == null) {
            throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND);
        }

        productViewService.record(id, clientIp);
        List<ReviewResponse> top3Reviews = reviewRepository.findTop3ByProductIdAndLikes(id);
        return response.withViewCountAndTop3Reviews(productViewService.getViewCount(id), top3Reviews);
    }


    // 재고 차감/복구 --------------------------------------------------------------------------------------
    @Caching(evict = {
            @CacheEvict(value = "productCache", allEntries = true, condition = "#result == true"),
            @CacheEvict(value = PRODUCT_SEARCH, allEntries = true, condition = "#result == true")
    })
    @Transactional
    public boolean decreaseStock(Long id, Long quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.decreaseStock(quantity);
        return product.getQuantity() == 0;
    }

    @Transactional
    public void increaseStock(Long id, Long quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.increaseStock(quantity);
    }

    //region RedissonLock 사용 재고 감소
    @CacheEvict(value = "productCache", allEntries = true, condition = "#result == true")
    @RedissonLock(key = "'stock:product:' + #id")
    public void decreaseStockWithRedisson(Long id, Long quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.decreaseStock(quantity);
    }

    @RedissonLock(key = "'stock:product:' + #id", waitTime = 10L)
    public void increaseStockWithRedisson(Long id, Long quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        product.increaseStock(quantity);
    }
    //endregion
}
