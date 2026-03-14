package one.theone.server.domain.freebieCategory.dto.response;

import one.theone.server.domain.freebieCategory.entity.FreebieCategoryDetail;

public record FreebieCategoryDetailUpdateResponse(
        Long id,
        String name,
        Integer sortNum
) {
    public static FreebieCategoryDetailUpdateResponse from(FreebieCategoryDetail detail) {
        return new FreebieCategoryDetailUpdateResponse(
                detail.getId(),
                detail.getName(),
                detail.getSortNum()
        );
    }
}
