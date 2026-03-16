package one.theone.server.domain.order.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.dto.response.OrderCreateResponse;
import one.theone.server.domain.order.dto.response.OrderDetailGetResponse;
import one.theone.server.domain.order.dto.response.OrderListGetResponse;
import one.theone.server.domain.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/direct")
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createDirectOrder(
            @RequestBody OrderCreateDirectRequest request
    ) {
        OrderCreateResponse response = orderService.createDirectOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "바로 구매 주문 생성 성공", response));
    }

    @PostMapping("/cart")
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createOrderFromCart(
            @RequestBody OrderCreateFromCartRequest request
    ) {
        OrderCreateResponse response = orderService.createOrderFromCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED.name(), "장바구니 주문 생성 성공", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<OrderListGetResponse>>> getOrderList(
            @RequestParam Long memberId
    ) {
        List<OrderListGetResponse> response = orderService.getOrderList(memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "주문 목록 조회 성공", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<BaseResponse<OrderDetailGetResponse>> getOrderList(
            @RequestParam Long memberId,
            @PathVariable Long orderId
    ) {
        OrderDetailGetResponse response = orderService.getOrderDetail(memberId, orderId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(HttpStatus.OK.name(), "주문 상세 조회 성공", response));
    }
}
