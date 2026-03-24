package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

public record RecentlyViewedProductsGetResponse(
        Long id,
        String name,
        Long price
) {
    public static RecentlyViewedProductsGetResponse from(Product product) {
        return new RecentlyViewedProductsGetResponse(product.getId(), product.getName(), product.getPrice());
    }
}
