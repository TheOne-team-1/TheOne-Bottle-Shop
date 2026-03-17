package one.theone.server.domain.favorite.service;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.favorite.dto.FavoriteRegisterResponse;
import one.theone.server.domain.favorite.dto.FavoritesGetResponse;
import one.theone.server.domain.favorite.entity.Favorite;
import one.theone.server.domain.favorite.repository.FavoriteRepository;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @InjectMocks
    private FavoriteService favoriteService;

    @Mock private FavoriteRepository favoriteRepository;
    @Mock private ProductRepository productRepository;

    @Test
    @DisplayName("즐겨찾기 등록 성공")
    void registerFavorite_success() {
        // given
        Long memberId = 1L;
        Long productId = 10L;

        given(productRepository.findById(productId)).willReturn(Optional.of(mock(Product.class)));
        given(favoriteRepository.existsByMemberIdAndProductId(memberId, productId)).willReturn(false);
        given(favoriteRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        // when
        FavoriteRegisterResponse response = favoriteService.registerFavorite(memberId, productId);

        // then
        assertThat(response.memberId()).isEqualTo(memberId);
        assertThat(response.productId()).isEqualTo(productId);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    @DisplayName("존재하지 않는 상품 즐겨찾기 등록 시 예외 발생")
    void registerFavorite_productNotFound() {
        // given
        Long memberId = 1L;
        Long productId = 10L;

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteService.registerFavorite(memberId, productId))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("이미 등록된 즐겨찾기 재등록 시 예외 발생")
    void registerFavorite_alreadyExists() {
        // given
        Long memberId = 1L;
        Long productId = 10L;

        given(productRepository.findById(productId)).willReturn(Optional.of(mock(Product.class)));
        given(favoriteRepository.existsByMemberIdAndProductId(memberId, productId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> favoriteService.registerFavorite(memberId, productId))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("즐겨찾기 삭제 성공")
    void deleteFavorite_success() {
        // given
        Long memberId = 1L;
        Long productId = 10L;
        Favorite favorite = Favorite.register(memberId, productId);

        given(favoriteRepository.findByMemberIdAndProductId(memberId, productId))
                .willReturn(Optional.of(favorite));

        // when
        favoriteService.deleteFavorite(memberId, productId);

        // then
        verify(favoriteRepository).delete(favorite);
    }

    @Test
    @DisplayName("존재하지 않는 즐겨찾기 삭제 시 예외 발생")
    void deleteFavorite_notFound() {
        // given
        Long memberId = 1L;
        Long productId = 10L;

        given(favoriteRepository.findByMemberIdAndProductId(memberId, productId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> favoriteService.deleteFavorite(memberId, productId))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - 필터 없이 전체 조회")
    void getFavorites_noFilter() {
        // given
        Long memberId = 1L;
        List<FavoritesGetResponse> responses = List.of(
                new FavoritesGetResponse(10L, FavoritesGetResponse.FavoriteProductStatus.SALES, LocalDateTime.now()),
                new FavoritesGetResponse(11L, FavoritesGetResponse.FavoriteProductStatus.DELETED, LocalDateTime.now())
        );

        given(favoriteRepository.findFavorites(any(), eq(memberId), eq(null)))
                .willReturn(new PageImpl<>(responses));

        // when
        PageResponse<FavoritesGetResponse> result = favoriteService.getFavorites(memberId, null, PageRequest.of(0, 10));

        // then
        assertThat(result.content()).hasSize(2);
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - SALES 필터")
    void getFavorites_salesFilter() {
        // given
        Long memberId = 1L;
        FavoritesGetResponse response = new FavoritesGetResponse(
                10L, FavoritesGetResponse.FavoriteProductStatus.SALES, LocalDateTime.now());

        given(favoriteRepository.findFavorites(any(), eq(memberId), eq(FavoritesGetResponse.FavoriteProductStatus.SALES)))
                .willReturn(new PageImpl<>(List.of(response)));

        // when
        PageResponse<FavoritesGetResponse> result = favoriteService.getFavorites(
                memberId, FavoritesGetResponse.FavoriteProductStatus.SALES, PageRequest.of(0, 10));

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status())
                .isEqualTo(FavoritesGetResponse.FavoriteProductStatus.SALES);
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 - DELETED 필터")
    void getFavorites_deletedFilter() {
        // given
        Long memberId = 1L;
        FavoritesGetResponse response = new FavoritesGetResponse(
                11L, FavoritesGetResponse.FavoriteProductStatus.DELETED, LocalDateTime.now());

        given(favoriteRepository.findFavorites(any(), eq(memberId), eq(FavoritesGetResponse.FavoriteProductStatus.DELETED)))
                .willReturn(new PageImpl<>(List.of(response)));

        // when
        PageResponse<FavoritesGetResponse> result = favoriteService.getFavorites(
                memberId, FavoritesGetResponse.FavoriteProductStatus.DELETED, PageRequest.of(0, 10));

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status())
                .isEqualTo(FavoritesGetResponse.FavoriteProductStatus.DELETED);
    }
}