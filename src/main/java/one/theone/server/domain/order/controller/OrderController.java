package one.theone.server.domain.order.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.dto.response.OrderCreateResponse;
import one.theone.server.domain.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
