package one.theone.server.domain.favorite.dto;

public record FavoriteRegisterResponse(
        Long id,
        Long memberId,
        Long productId
) {}
