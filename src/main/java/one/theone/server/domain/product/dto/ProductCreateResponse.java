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
        String productCategoryDetailName,
        Long quantity
) {
    public static ProductCreateResponse of(Product product, String productCategoryDetailName) {
        return new ProductCreateResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus(),
                product.getAbv(),
                product.getVolumeMl(),
                productCategoryDetailName,
                product.getQuantity()
        );
    }
}
