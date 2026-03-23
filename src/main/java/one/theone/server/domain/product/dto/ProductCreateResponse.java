package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

import java.math.BigDecimal;

public record ProductCreateResponse(
        Long id,
        String name,
        Long price,
        Product.ProductStatus status,
        BigDecimal abv,
        int volumeMl,
        Long productCategoryDetailId,
        Long quantity
) {
    public static ProductCreateResponse from(Product product) {
        return new ProductCreateResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus(),
                product.getAbv(),
                product.getVolumeMl(),
                product.getCategoryDetailId(),
                product.getQuantity()
        );
    }
}
