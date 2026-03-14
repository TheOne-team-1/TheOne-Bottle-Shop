package one.theone.server.domain.freebie.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.freebie.dto.response.FreebieGetResponse;
import one.theone.server.domain.freebie.dto.response.FreebiesGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static one.theone.server.domain.freebie.entity.QFreebie.freebie;

@RequiredArgsConstructor
public class FreebieQueryRepositoryImpl implements FreebieQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FreebiesGetResponse> findAllFreebies(Pageable pageable) {
        List<FreebiesGetResponse> result = queryFactory
                .select(Projections.constructor(FreebiesGetResponse.class,
                        freebie.id,
                        freebie.name,
                        freebie.quantity,
                        freebie.status))
                .from(freebie)
                .where(freebie.deleted.isFalse())
                .orderBy(freebie.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(freebie.count())
                .from(freebie)
                .where(freebie.deleted.isFalse())
                .fetchOne();

        return new PageImpl<>(result, pageable, total == null ? 0L : total);
    }

    @Override
    public FreebieGetResponse findFreebieById(Long id) {
        return queryFactory
                .select(Projections.constructor(FreebieGetResponse.class,
                        freebie.id,
                        freebie.freebieCategoryDetailId,
                        freebie.name,
                        freebie.quantity,
                        freebie.status))
                .from(freebie)
                .where(
                        freebie.id.eq(id),
                        freebie.deleted.isFalse())
                .fetchOne();
    }
}
