package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

import java.math.BigDecimal;

public record ProductsGetResponse(
        Long id,
        String name,
        Long price,
        Product.ProductStatus status,
        BigDecimal rating
) {}
