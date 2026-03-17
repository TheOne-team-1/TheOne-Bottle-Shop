package one.theone.server.domain.favorite.dto;

import java.time.LocalDateTime;

public record FavoritesGetResponse(
        Long productId,
        FavoriteProductStatus status,
        LocalDateTime createdAt
) {
    public enum FavoriteProductStatus {
        SALES, SOLD_OUT, DISCONTINUE, DELETED
    }
}
