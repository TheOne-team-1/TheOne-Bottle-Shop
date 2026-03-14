package one.theone.server.domain.freebieCategory.dto.response;

import one.theone.server.domain.freebieCategory.entity.FreebieCategory;

import java.time.LocalDateTime;

public record FreebieCategoryDeleteResponse(
        Long id,
        String name,
        Boolean deleted,
        LocalDateTime deletedAt
) {
    public static FreebieCategoryDeleteResponse from(FreebieCategory freebieCategory) {
        return new FreebieCategoryDeleteResponse(
                freebieCategory.getId(),
                freebieCategory.getName(),
                freebieCategory.getDeleted(),
                freebieCategory.getDeletedAt()
        );
    }
}
