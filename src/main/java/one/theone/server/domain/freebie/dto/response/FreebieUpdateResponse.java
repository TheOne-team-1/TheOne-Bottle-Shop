package one.theone.server.domain.freebie.dto.response;

import one.theone.server.domain.freebie.entity.Freebie;

public record FreebieUpdateResponse(
        Long id,
        Long freebieCategoryDetailId,
        String name,
        Long quantity,
        Freebie.FreebieStatus status
) {}
