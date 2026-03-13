package one.theone.server.domain.category.dto;

import one.theone.server.domain.category.entity.Category;

public record CategoryUpdateResponse(
        Long id,
        String name,
        Integer sortNum
) {
    public static CategoryUpdateResponse from(Category category) {
        return new CategoryUpdateResponse(
                category.getId(),
                category.getName(),
                category.getSortNum()
        );
    }
}