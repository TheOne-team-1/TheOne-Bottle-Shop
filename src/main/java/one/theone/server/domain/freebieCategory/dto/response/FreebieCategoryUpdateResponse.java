package one.theone.server.domain.freebieCategory.dto.response;

import one.theone.server.domain.freebieCategory.entity.FreebieCategory;

public record FreebieCategoryUpdateResponse(
        Long id,
        String name,
        Integer sortNum
) {
    public static FreebieCategoryUpdateResponse from(FreebieCategory freebieCategory) {
        return new FreebieCategoryUpdateResponse(
                freebieCategory.getId(),
                freebieCategory.getName(),
                freebieCategory.getSortNum()
        );
    }
}
