package one.theone.server.domain.freebieCategory.dto.response;

import one.theone.server.domain.freebieCategory.entity.FreebieCategoryDetail;

public record FreebieCategoryDetailCreateResponse(
        Long id,
        Long freebieCategoryId,
        String name,
        Integer sortNum
) {
    public static FreebieCategoryDetailCreateResponse from(FreebieCategoryDetail detail) {
        return new FreebieCategoryDetailCreateResponse(
                detail.getId(),
                detail.getFreebieCategoryId(),
                detail.getName(),
                detail.getSortNum()
        );
    }
}
