package one.theone.server.domain.event.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository{

    private final JPAQueryFactory queryFactory;
}
