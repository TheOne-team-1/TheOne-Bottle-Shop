package one.theone.server.domain.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.dto.response.*;
import one.theone.server.domain.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/direct")
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createDirectOrder(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody OrderCreateDirectRequest request
    ) {
        OrderCreateResponse response = orderService.createDirectOrder(memberId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "바로 구매 주문 생성 성공", response));
    }

    @PostMapping("/cart")
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createOrderFromCart(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody OrderCreateFromCartRequest request
    ) {
        OrderCreateResponse response = orderService.createOrderFromCart(memberId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "장바구니 주문 생성 성공", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<OrderPageResponse>> getOrderList(
            @AuthenticationPrincipal Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        OrderPageResponse response = orderService.getOrderList(memberId, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "주문 목록 조회 성공", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<BaseResponse<OrderDetailGetResponse>> getOrderDetail(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        OrderDetailGetResponse response = orderService.getOrderDetail(memberId, orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "주문 상세 조회 성공", response));
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<BaseResponse<OrderCancelResponse>> cancelOrder(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long orderId
    ) {
        OrderCancelResponse response = orderService.cancelOrder(memberId, orderId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "주문 취소 성공", response));
    }
}
