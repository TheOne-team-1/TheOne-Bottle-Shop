package one.theone.server.domain.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminProductsGetResponse(
        Long id,
        String name,
        Long price,
        AdminProductsStatus status,
        Long quantity,
        BigDecimal rating,
        LocalDateTime createdAt
) {
    public enum AdminProductsStatus {
        SALES, SOLD_OUT, DISCONTINUE, DELETED
    }
}