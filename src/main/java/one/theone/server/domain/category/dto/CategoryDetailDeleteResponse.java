package one.theone.server.domain.category.dto;

import one.theone.server.domain.category.entity.CategoryDetail;

import java.time.LocalDateTime;

public record CategoryDetailDeleteResponse(
        Long id,
        String name,
        Boolean deleted,
        LocalDateTime deletedAt
) {
    public static CategoryDetailDeleteResponse from(CategoryDetail categoryDetail) {
        return new CategoryDetailDeleteResponse(
                categoryDetail.getId(),
                categoryDetail.getName(),
                categoryDetail.getDeleted(),
                categoryDetail.getDeletedAt()
        );
    }
}