package one.theone.server.domain.freebie.dto.response;

import one.theone.server.domain.freebie.entity.Freebie;

public record FreebieGetResponse(
        Long id,
        Long freebieCategoryDetailId,
        String name,
        Long quantity,
        Freebie.FreebieStatus status
) {}
