package one.theone.server.domain.category.dto;

import one.theone.server.domain.category.entity.Category;

public record CategoryCreateResponse(
        Long id,
        String name,
        int sortNum
) {
    public static CategoryCreateResponse from(Category category) {
        return new CategoryCreateResponse(
                category.getId(),
                category.getName(),
                category.getSortNum()
        );
    }
}
