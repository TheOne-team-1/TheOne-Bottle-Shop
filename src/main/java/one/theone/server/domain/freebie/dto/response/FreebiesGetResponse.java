package one.theone.server.domain.freebie.dto.response;

import one.theone.server.domain.freebie.entity.Freebie;

public record FreebiesGetResponse(
        Long id,
        String name,
        Long quantity,
        Freebie.FreebieStatus status
) {}
