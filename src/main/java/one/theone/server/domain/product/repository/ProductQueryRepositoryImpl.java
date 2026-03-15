package one.theone.server.domain.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.ProductGetResponse;
import one.theone.server.domain.product.dto.ProductsGetRequest;
import one.theone.server.domain.product.dto.ProductsGetResponse;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.math.BigDecimal;
import java.util.List;

import static one.theone.server.domain.product.entity.QProduct.product;
import static one.theone.server.domain.category.entity.QCategory.category;
import static one.theone.server.domain.category.entity.QCategoryDetail.categoryDetail;

@RequiredArgsConstructor
public class ProductQueryRepositoryImpl implements ProductQueryRepository{

    private final JPAQueryFactory queryFactory;

    private BooleanExpression buildCondition(List<String> keywordMorphemes, String keyword) {
        if (keywordMorphemes.isEmpty()) return null;

        BooleanExpression condition = null;
        for (String morpheme : keywordMorphemes) {
            BooleanExpression morphemeCondition = product.name.containsIgnoreCase(morpheme)
                    .or(category.name.containsIgnoreCase(morpheme))
                    .or(categoryDetail.name.containsIgnoreCase(morpheme));

            condition = condition == null ? morphemeCondition : condition.or(morphemeCondition);
        }

        boolean isDifferent = keywordMorphemes.size() != 1 || !keywordMorphemes.getFirst().equals(keyword);
        if (isDifferent) {
            BooleanExpression originalCondition = product.name.containsIgnoreCase(keyword)
                    .or(category.name.containsIgnoreCase(keyword))
                    .or(categoryDetail.name.containsIgnoreCase(keyword));
            condition = condition.or(originalCondition);
        }

        return condition;
    }

    @Override
    public PageResponse<ProductSearchResponse> findProductByKeyword(List<String> keywordMorphemes, String keyword, Pageable pageable) {
        BooleanExpression condition = buildCondition(keywordMorphemes, keyword);

        List<ProductSearchResponse> content = queryFactory
                .select(Projections.constructor(ProductSearchResponse.class,
                        product.name,
                        product.price,
                        categoryDetail.name,
                        category.name))
                .from(product)
                .leftJoin(categoryDetail).on(product.categoryDetailId.eq(categoryDetail.id))
                .leftJoin(category).on(categoryDetail.categoryId.eq(category.id))
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .leftJoin(categoryDetail).on(product.categoryDetailId.eq(categoryDetail.id))
                .leftJoin(category).on(categoryDetail.categoryId.eq(category.id))
                .where(condition);

        Page<ProductSearchResponse> page =
                PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);

        return PageResponse.register(page);
    }

    @Override
    public Page<ProductsGetResponse> findProductWithConditions(Pageable pageable, ProductsGetRequest request) {
        List<ProductsGetResponse> result = queryFactory
                .select(Projections.constructor(ProductsGetResponse.class,
                        product.id,
                        product.name,
                        product.price,
                        product.status,
                        product.rating))
                .from(product)
                .where(
                        product.deleted.isFalse(),
                        product.status.in(Product.ProductStatus.SALES, Product.ProductStatus.SOLD_OUT),
                        categoryIn(request.categoryIds()),
                        abvBetween(request.abvMin(), request.abvMax()),
                        priceBetween(request.priceMin(), request.priceMax()),
                        volumeIn(request.volumeMl())
                )
                .orderBy(getOrderSpecifier(request.sortType()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(
                        product.deleted.isFalse(),
                        product.status.in(Product.ProductStatus.SALES, Product.ProductStatus.SOLD_OUT),
                        categoryIn(request.categoryIds()),
                        abvBetween(request.abvMin(), request.abvMax()),
                        priceBetween(request.priceMin(), request.priceMax()),
                        volumeIn(request.volumeMl())
                )
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(result, pageable, total);
    }

    @Override
    public ProductGetResponse findProductById(Long id) {
        return queryFactory
                .select(Projections.constructor(ProductGetResponse.class,
                        product.id,
                        product.name,
                        product.price,
                        product.status,
                        product.abv,
                        product.volumeMl,
                        categoryDetail.categoryId,
                        product.categoryDetailId,
                        product.quantity,
                        product.rating,
                        Expressions.constant(0L)))
                .from(product)
                .leftJoin(categoryDetail).on(product.categoryDetailId.eq(categoryDetail.id))
                .where(
                        product.id.eq(id),
                        product.deleted.isFalse())
                .fetchOne();
    }

    private BooleanExpression categoryIn(List<Long> categoryIds) {
        return categoryIds != null && !categoryIds.isEmpty() ? product.categoryDetailId.in(categoryIds) : null;
    }

    private BooleanExpression abvBetween(BigDecimal abvMin, BigDecimal abvMax) {
        if (abvMin != null && abvMax != null) {
            return product.abv.between(abvMin, abvMax);
        } else if (abvMin != null) {
            return product.abv.goe(abvMin);
        } else if (abvMax != null) {
            return product.abv.loe(abvMax);
        }
        return null;
    }

    private BooleanExpression priceBetween(Long priceMin, Long priceMax) {
        if (priceMin != null && priceMax != null) {
            return product.price.between(priceMin, priceMax);
        } else if (priceMin != null) {
            return product.price.goe(priceMin);
        } else if (priceMax != null) {
            return product.price.loe(priceMax);
        }
        return null;
    }

    private BooleanExpression volumeIn(List<Integer> volumeMl) {
        return volumeMl != null && !volumeMl.isEmpty() ? product.volumeMl.in(volumeMl) : null;
    }

    private OrderSpecifier<?> getOrderSpecifier(ProductsGetRequest.ProductSortType sortType) {
        return switch (sortType) {
            case LATEST -> product.createdAt.desc();
            case PRICE_ASC -> product.price.asc();
            case PRICE_DESC -> product.price.desc();
            case RATING_DESC -> product.rating.desc();
        };
    }
}
