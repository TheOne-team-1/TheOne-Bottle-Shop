package one.theone.server.domain.freebieCategory.dto.response;

import one.theone.server.domain.freebieCategory.entity.FreebieCategoryDetail;

import java.time.LocalDateTime;

public record FreebieCategoryDetailDeleteResponse(
        Long id,
        String name,
        Boolean deleted,
        LocalDateTime deletedAt
) {
    public static FreebieCategoryDetailDeleteResponse from(FreebieCategoryDetail detail) {
        return new FreebieCategoryDetailDeleteResponse(
                detail.getId(),
                detail.getName(),
                detail.getDeleted(),
                detail.getDeletedAt()
        );
    }
}
