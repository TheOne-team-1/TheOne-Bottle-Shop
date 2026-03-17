package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

public record BestProductsGetResponse(
        Long id,
        String name,
        Long price
) {
    public static BestProductsGetResponse from(Product product) {
        return new BestProductsGetResponse(product.getId(), product.getName(), product.getPrice());
    }
}
