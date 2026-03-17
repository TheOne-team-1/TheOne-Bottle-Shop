package one.theone.server.domain.favorite.repository;

import one.theone.server.domain.favorite.dto.FavoritesGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteQueryRepository {

    Page<FavoritesGetResponse> findFavorites(Pageable pageable, Long memberId, FavoritesGetResponse.FavoriteProductStatus status);
}
