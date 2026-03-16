package one.theone.server.domain.freebieCategory.dto.response;

import one.theone.server.domain.freebieCategory.entity.FreebieCategory;
import one.theone.server.domain.freebieCategory.entity.FreebieCategoryDetail;

import java.util.List;

public record FreebieCategoriesGetResponse(
        Long id,
        String name,
        Integer sortNum,
        List<FreebieCategoryDetailInfo> details
) {
    public record FreebieCategoryDetailInfo(
            Long id,
            String name,
            Integer sortNum
    ) {}

    public static FreebieCategoriesGetResponse of(FreebieCategory freebieCategory, List<FreebieCategoryDetail> details) {
        return new FreebieCategoriesGetResponse(
                freebieCategory.getId(),
                freebieCategory.getName(),
                freebieCategory.getSortNum(),
                details.stream()
                        .map(d -> new FreebieCategoryDetailInfo(d.getId(), d.getName(), d.getSortNum()))
                        .toList()
        );
    }
}
