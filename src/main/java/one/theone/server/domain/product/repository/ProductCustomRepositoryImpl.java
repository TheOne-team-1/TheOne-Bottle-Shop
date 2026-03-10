package one.theone.server.domain.product.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.product.dto.ProductsGetRequest;
import one.theone.server.domain.product.dto.ProductsGetResponse;
import one.theone.server.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static one.theone.server.domain.product.entity.QProduct.product;

@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductsGetResponse> findAllWithConditions(Pageable pageable, ProductsGetRequest request) {
        List<ProductsGetResponse> result = queryFactory
                .select(Projections.constructor(ProductsGetResponse.class,
                        product.id,
                        product.name,
                        product.price,
                        product.status,
                        product.rating))
                .from(product)
                .where(
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

    private BooleanExpression categoryIn(List<Long> categoryIds) {
        return categoryIds != null && !categoryIds.isEmpty() ? product.productCategoryDetailId.in(categoryIds) : null;
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
