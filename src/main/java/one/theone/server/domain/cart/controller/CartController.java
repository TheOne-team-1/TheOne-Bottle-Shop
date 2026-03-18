package one.theone.server.domain.cart.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.cart.dto.request.CartAddRequest;
import one.theone.server.domain.cart.dto.request.CartUpdateQuantityRequest;
import one.theone.server.domain.cart.dto.response.*;
import one.theone.server.domain.cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<BaseResponse<CartAddResponse>> addItem(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CartAddRequest request
    ) {
        CartAddResponse response = cartService.addItem(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "장바구니 상품 등록 성공", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> getCart(
            @AuthenticationPrincipal Long memberId
    ) {
        CartResponse response = cartService.getCart(memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "장바구니 조회 성공", response));
    }

    @PatchMapping("/items/{productId}")
    public ResponseEntity<BaseResponse<CartUpdateQuantityResponse>> updateCart(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId,
            @Valid @RequestBody CartUpdateQuantityRequest request
    ) {
        CartUpdateQuantityResponse response = cartService.updateQuantity(memberId, productId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "장바구니 수량 변경 성공", response));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<BaseResponse<CartRemoveItemResponse>> removeItem(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long productId
    ) {
        CartRemoveItemResponse response = cartService.removeItem(memberId, productId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "장바구니 상품 삭제 성공", response));
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<CartRemoveResponse>> removeCart(
            @AuthenticationPrincipal Long memberId
    ) {
        CartRemoveResponse response = cartService.removeCart(memberId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "장바구니 삭제 성공", response));
    }
}
