package one.theone.server.domain.favorite.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.FavoriteExceptionEnum;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.favorite.dto.FavoritesGetResponse;
import one.theone.server.domain.favorite.entity.Favorite;
import one.theone.server.domain.favorite.repository.FavoriteRepository;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Void registerFavorite(Long memberId, Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND));

        if (favoriteRepository.existsByMemberIdAndProductId(memberId, productId)) {
            throw new ServiceErrorException(FavoriteExceptionEnum.ERR_FAVORITE_ALREADY_REGISTER);
        }

        Favorite favorite = Favorite.register(memberId, productId);
        favoriteRepository.save(favorite);

        return null;
    }

    @Transactional
    public Void deleteFavorite(Long memberId, Long productId) {
        Favorite favorite = favoriteRepository.findByMemberIdAndProductId(memberId, productId)
                .orElseThrow(() -> new ServiceErrorException(FavoriteExceptionEnum.ERR_FAVORITE_NOT_FOUND));

        favoriteRepository.delete(favorite);

        return null;
    }

    @Transactional(readOnly = true)
    public PageResponse<FavoritesGetResponse> getFavorites(Long memberId, FavoritesGetResponse.FavoriteProductStatus status, Pageable pageable) {
        Page<FavoritesGetResponse> page = favoriteRepository.findFavorites(pageable, memberId, status);
        return PageResponse.register(page);
    }
}
