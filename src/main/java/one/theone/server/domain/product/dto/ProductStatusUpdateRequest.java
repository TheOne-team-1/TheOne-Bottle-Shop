package one.theone.server.domain.product.dto;

import jakarta.validation.constraints.NotNull;
import one.theone.server.domain.product.entity.Product;

public record ProductStatusUpdateRequest(
        @NotNull(message = "상품 상태는 필수입니다")
        Product.ProductStatus status
) {}
