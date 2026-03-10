package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.product.dto.ProductCreateRequest;
import one.theone.server.domain.product.dto.ProductCreateResponse;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.entity.ProductCategoryDetail;
import one.theone.server.domain.product.repository.ProductCategoryDetailRepository;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryDetailRepository productCategoryDetailRepository;

    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request) {
        ProductCategoryDetail productCategoryDetail = productCategoryDetailRepository.findById(request.productCategoryDetailId())
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_CATEGORY_NOT_FOUND));

        Product product = Product.register(
                request.name(),
                request.price(),
                request.abv(),
                request.volumeMl(),
                request.productCategoryDetailId(),
                request.quantity()
        );

        productRepository.save(product);
        return ProductCreateResponse.of(product, productCategoryDetail.getName());
    }
}
