package one.theone.server.domain.favorite.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.favorite.dto.FavoriteRegisterResponse;
import one.theone.server.domain.favorite.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
