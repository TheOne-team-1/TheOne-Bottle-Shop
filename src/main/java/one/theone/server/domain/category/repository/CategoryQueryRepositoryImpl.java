package one.theone.server.domain.category.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.category.dto.CategoriesGetResponse;
import one.theone.server.domain.category.entity.Category;
import one.theone.server.domain.category.entity.CategoryDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static one.theone.server.domain.category.entity.QCategory.category;
import static one.theone.server.domain.category.entity.QCategoryDetail.categoryDetail;

@RequiredArgsConstructor
public class CategoryQueryRepositoryImpl implements CategoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CategoriesGetResponse> findAllCategories(Pageable pageable) {
        List<Category> categories = queryFactory
                .selectFrom(category)
                .where(category.deleted.isFalse())
                .orderBy(category.sortNum.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(category.count())
                .from(category)
                .where(category.deleted.isFalse())
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        // 대분류 카테고리 아이디 추출
        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();

        // 소분류 카테고리 조회
        List<CategoryDetail> categoryDetails = queryFactory
                .selectFrom(categoryDetail)
                .where(
                        categoryDetail.categoryId.in(categoryIds),
                        categoryDetail.deleted.isFalse()
                )
                .orderBy(categoryDetail.sortNum.asc())
                .fetch();

        // 그룹핑
        Map<Long, List<CategoryDetail>> detailMap = categoryDetails.stream()
                .collect(Collectors.groupingBy(CategoryDetail::getCategoryId));

        List<CategoriesGetResponse> result = categories.stream()
                .map(c -> CategoriesGetResponse.of(c, detailMap.getOrDefault(c.getId(), List.of())))
                .toList();

        return new PageImpl<>(result, pageable, total);
    }
}
