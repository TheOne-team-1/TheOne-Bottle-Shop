package one.theone.server.domain.freebie.dto.response;

import java.time.LocalDateTime;

public record FreebieDeleteResponse(
        Long id,
        String name,
        Boolean deleted,
        LocalDateTime deletedAt
) {}
