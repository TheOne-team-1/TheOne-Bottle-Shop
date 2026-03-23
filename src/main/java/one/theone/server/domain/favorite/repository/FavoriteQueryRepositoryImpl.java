package one.theone.server.domain.favorite.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import one.theone.server.domain.favorite.dto.FavoritesGetResponse;
import one.theone.server.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static one.theone.server.domain.favorite.entity.QFavorite.favorite;
import static one.theone.server.domain.product.entity.QProduct.product;

@RequiredArgsConstructor
public class FavoriteQueryRepositoryImpl implements FavoriteQueryRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FavoritesGetResponse> findFavorites(Pageable pageable, Long memberId, FavoritesGetResponse.FavoriteProductStatus status) {
        List<FavoritesGetResponse> result = queryFactory
                .select(Projections.constructor(FavoritesGetResponse.class,
                        favorite.productId,
                        productStatusExpression(),
                        favorite.createdAt))
                .from(favorite)
                .leftJoin(product).on(favorite.productId.eq(product.id))
                .where(
                        favorite.memberId.eq(memberId),
                        statusEq(status)
                )
                .orderBy(favorite.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(favorite.count())
                .from(favorite)
                .leftJoin(product).on(favorite.productId.eq(product.id))
                .where(
                        favorite.memberId.eq(memberId),
                        statusEq(status)
                )
                .fetchOne();

        if (total == null) {
            total = 0L;
        }

        return new PageImpl<>(result, pageable, total);
    }

    private Expression<FavoritesGetResponse.FavoriteProductStatus> productStatusExpression() {
        return new CaseBuilder()
                .when(product.deleted.isTrue())
                .then(FavoritesGetResponse.FavoriteProductStatus.DELETED)
                .when(product.status.eq(Product.ProductStatus.SALES))
                .then(FavoritesGetResponse.FavoriteProductStatus.SALES)
                .when(product.status.eq(Product.ProductStatus.SOLD_OUT))
                .then(FavoritesGetResponse.FavoriteProductStatus.SOLD_OUT)
                .otherwise(FavoritesGetResponse.FavoriteProductStatus.DISCONTINUE);
    }

    private BooleanExpression statusEq(FavoritesGetResponse.FavoriteProductStatus status) {
        if (status == null) return null;

        return switch (status) {
            case SALES -> product.deleted.isFalse().and(product.status.eq(Product.ProductStatus.SALES));
            case SOLD_OUT -> product.deleted.isFalse().and(product.status.eq(Product.ProductStatus.SOLD_OUT));
            case DISCONTINUE -> product.deleted.isFalse().and(product.status.eq(Product.ProductStatus.DISCONTINUE));
            case DELETED -> product.deleted.isTrue();
        };
    }
}
