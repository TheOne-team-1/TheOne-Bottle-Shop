package one.theone.server.domain.freebieCategory.dto.response;

import one.theone.server.domain.freebieCategory.entity.FreebieCategory;

public record FreebieCategoryCreateResponse(
        Long id,
        String name,
        Integer sortNum
) {
    public static FreebieCategoryCreateResponse from(FreebieCategory freebieCategory) {
        return new FreebieCategoryCreateResponse(
                freebieCategory.getId(),
                freebieCategory.getName(),
                freebieCategory.getSortNum()
        );
    }
}
