package one.theone.server.domain.category.dto;

import one.theone.server.domain.category.entity.Category;
import one.theone.server.domain.category.entity.CategoryDetail;

import java.util.List;

public record CategoriesGetResponse(
        Long id,
        String name,
        Integer sortNum,
        List<CategoryDetailInfo> details
) {
    public record CategoryDetailInfo(
            Long id,
            String name,
            Integer sortNum
    ) {}

    public static CategoriesGetResponse of(Category category, List<CategoryDetail> categoryDetails) {
        return new CategoriesGetResponse(
                category.getId(),
                category.getName(),
                category.getSortNum(),
                categoryDetails.stream()
                        .map(cd -> new CategoryDetailInfo(cd.getId(), cd.getName(), cd.getSortNum()))
                        .toList()
        );
    }
}
