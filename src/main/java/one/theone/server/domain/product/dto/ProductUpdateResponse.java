package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

import java.math.BigDecimal;

public record ProductUpdateResponse(
        Long id,
        String name,
        Long price,
        BigDecimal abv,
        int volumeMl,
        Long productCategoryDetailId,
        Long quantity
) {
    public static ProductUpdateResponse from(Product product) {
        return new ProductUpdateResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getAbv(),
                product.getVolumeMl(),
                product.getCategoryDetailId(),
                product.getQuantity()
        );
    }
}