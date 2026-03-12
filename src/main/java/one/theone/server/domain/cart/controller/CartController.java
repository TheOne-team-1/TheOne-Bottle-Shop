package one.theone.server.domain.cart.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.cart.dto.request.CartAddRequest;
import one.theone.server.domain.cart.dto.response.CartAddResponse;
import one.theone.server.domain.cart.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<BaseResponse<CartAddResponse>> addItem(
            @RequestParam Long memberId,
            @RequestBody CartAddRequest request) {
        CartAddResponse response = cartService.addItem(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "장바구니 상품 등록 성공", response));
    }
}
