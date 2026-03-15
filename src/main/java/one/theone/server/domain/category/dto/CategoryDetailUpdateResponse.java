package one.theone.server.domain.category.dto;

import one.theone.server.domain.category.entity.CategoryDetail;

public record CategoryDetailUpdateResponse(
        Long id,
        String name,
        Integer sortNum
) {
    public static CategoryDetailUpdateResponse from(CategoryDetail categoryDetail) {
        return new CategoryDetailUpdateResponse(
                categoryDetail.getId(),
                categoryDetail.getName(),
                categoryDetail.getSortNum()
        );
    }
}