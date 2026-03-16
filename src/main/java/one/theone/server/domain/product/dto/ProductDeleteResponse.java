package one.theone.server.domain.product.dto;

import one.theone.server.domain.product.entity.Product;

import java.time.LocalDateTime;

public record ProductDeleteResponse(
        Long id,
        String name,
        Boolean deleted,
        LocalDateTime deletedAt
) {
    public static ProductDeleteResponse from(Product product) {
        return new ProductDeleteResponse(
                product.getId(),
                product.getName(),
                product.getDeleted(),
                product.getDeletedAt()
        );
    }
}
