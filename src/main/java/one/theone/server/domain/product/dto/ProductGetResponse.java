package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

import java.math.BigDecimal;

public record ProductGetResponse(
        Long id,
        String name,
        Long price,
        Product.ProductStatus status,
        BigDecimal abv,
        int volumeMl,
        Long categoryId,
        Long categoryDetailId,
        Long quantity,
        BigDecimal rating,
        Long viewCount
) {
    public ProductGetResponse withViewCount(Long viewCount) {
        return new ProductGetResponse(id, name,price, status, abv, volumeMl, categoryId, categoryDetailId, quantity, rating, viewCount);
    }
}
