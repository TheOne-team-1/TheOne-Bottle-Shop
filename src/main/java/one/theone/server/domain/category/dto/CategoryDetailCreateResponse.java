package one.theone.server.domain.category.dto;

import one.theone.server.domain.category.entity.CategoryDetail;

public record CategoryDetailCreateResponse(
        Long id,
        Long categoryId,
        String name,
        Integer sortNum
) {
    public static CategoryDetailCreateResponse from(CategoryDetail categoryDetail) {
        return new CategoryDetailCreateResponse(
                categoryDetail.getId(),
                categoryDetail.getCategoryId(),
                categoryDetail.getName(),
                categoryDetail.getSortNum()
        );
    }
}