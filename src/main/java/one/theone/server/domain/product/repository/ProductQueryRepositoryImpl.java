package one.theone.server.domain.product.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static one.theone.server.domain.product.entity.QProduct.product;
import static one.theone.server.domain.product.entity.QProductCategory.productCategory;
import static one.theone.server.domain.product.entity.QProductCategoryDetail.productCategoryDetail;

@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository{

    private final JPAQueryFactory queryFactory;

    private BooleanExpression buildCondition(String keyword) {
        if (!StringUtils.hasText(keyword)) return null;

        return product.name.containsIgnoreCase(keyword)
                .or(productCategory.name.containsIgnoreCase(keyword))
                .or(productCategoryDetail.name.containsIgnoreCase(keyword));
    }

    @Override
    public PageResponse<ProductSearchResponse> findProductByKeyword(String keyword, Pageable pageable) {
        BooleanExpression condition = buildCondition(keyword);

        List<ProductSearchResponse> content = queryFactory
                .select(Projections.constructor(ProductSearchResponse.class,
                        product.name,
                        product.price,
                        productCategoryDetail.name,
                        productCategory.name))
                .from(product)
                .leftJoin(productCategoryDetail).on(product.productCategoryDetailId.eq(productCategoryDetail.id))
                .leftJoin(productCategory).on(productCategoryDetail.productCategoryId.eq(productCategory.id))
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(productCategoryDetail).on(product.productCategoryDetailId.eq(productCategoryDetail.id))
                .leftJoin(productCategory).on(productCategoryDetail.productCategoryId.eq(productCategory.id))
                .where(condition);

        Page<ProductSearchResponse> page =
                PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

        return PageResponse.register(page);
    }
}
