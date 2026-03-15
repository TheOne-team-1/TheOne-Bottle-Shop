package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

public record ProductStatusUpdateResponse(
        Long id,
        Product.ProductStatus status
) {
    public static ProductStatusUpdateResponse from(Product product) {
        return new ProductStatusUpdateResponse(
                product.getId(),
                product.getStatus()
        );
    }
}
