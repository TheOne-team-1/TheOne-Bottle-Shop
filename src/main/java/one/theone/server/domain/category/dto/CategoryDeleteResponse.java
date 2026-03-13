package one.theone.server.domain.category.dto;

import one.theone.server.domain.category.entity.Category;

import java.time.LocalDateTime;

public record CategoryDeleteResponse(
        Long id,
        String name,
        Boolean deleted,
        LocalDateTime deletedAt
) {
    public static CategoryDeleteResponse from(Category category) {
        return new CategoryDeleteResponse(
                category.getId(),
                category.getName(),
                category.getDeleted(),
                category.getDeletedAt()
        );
    }
}
