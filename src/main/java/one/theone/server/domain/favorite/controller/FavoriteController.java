package one.theone.server.domain.favorite.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.favorite.dto.FavoriteRegisterResponse;
import one.theone.server.domain.favorite.dto.FavoritesGetResponse;
import one.theone.server.domain.favorite.service.FavoriteService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public ResponseEntity<BaseResponse<FavoriteRegisterResponse>> registerFavorite(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "즐겨찾기 등록 성공", favoriteService.registerFavorite(memberId, productId)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<BaseResponse<Void>> deleteFavorite(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "즐겨찾기 삭제 성공", favoriteService.deleteFavorite(memberId, productId)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<FavoritesGetResponse>>> getFavorites(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(required = false) FavoritesGetResponse.FavoriteProductStatus status,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "즐겨찾기 목록 조회 성공", favoriteService.getFavorites(memberId, status, pageable)));
    }
}
