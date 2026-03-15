package one.theone.server.domain.freebieCategory.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.freebieCategory.dto.response.FreebieCategoriesGetResponse;
import one.theone.server.domain.freebieCategory.entity.FreebieCategory;
import one.theone.server.domain.freebieCategory.entity.FreebieCategoryDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static one.theone.server.domain.freebieCategory.entity.QFreebieCategory.freebieCategory;
import static one.theone.server.domain.freebieCategory.entity.QFreebieCategoryDetail.freebieCategoryDetail;

@RequiredArgsConstructor
public class FreebieCategoryQueryRepositoryImpl implements FreebieCategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FreebieCategoriesGetResponse> findAllFreebieCategories(Pageable pageable) {
        List<FreebieCategory> categories = queryFactory
                .selectFrom(freebieCategory)
                .where(freebieCategory.deleted.isFalse())
                .orderBy(freebieCategory.sortNum.asc().nullsLast(), freebieCategory.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(freebieCategory.count())
                .from(freebieCategory)
                .where(freebieCategory.deleted.isFalse())
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        List<Long> categoryIds = categories.stream()
                .map(FreebieCategory::getId)
                .toList();

        List<FreebieCategoryDetail> details = queryFactory
                .selectFrom(freebieCategoryDetail)
                .where(
                        freebieCategoryDetail.freebieCategoryId.in(categoryIds),
                        freebieCategoryDetail.deleted.isFalse()
                )
                .orderBy(freebieCategoryDetail.sortNum.asc().nullsLast(), freebieCategoryDetail.id.asc())
                .fetch();

        Map<Long, List<FreebieCategoryDetail>> detailMap = details.stream()
                .collect(Collectors.groupingBy(FreebieCategoryDetail::getFreebieCategoryId));

        List<FreebieCategoriesGetResponse> result = categories.stream()
                .map(c -> FreebieCategoriesGetResponse.of(c, detailMap.getOrDefault(c.getId(), List.of())))
                .toList();

        return new PageImpl<>(result, pageable, total);
    }
}
