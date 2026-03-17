package one.theone.server.domain.favorite.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.favorite.dto.FavoriteRegisterResponse;
import one.theone.server.domain.favorite.service.FavoriteService;
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
}
