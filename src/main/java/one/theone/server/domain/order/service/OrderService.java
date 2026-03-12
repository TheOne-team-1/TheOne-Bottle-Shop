package one.theone.server.domain.order.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.OrderExceptionEnum;
import one.theone.server.domain.order.dto.request.OrderCreateRequest;
import one.theone.server.domain.order.dto.response.OrderCreateResponse;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        Long totalAmount = calculateTotalAmount(request);
        Long discountAmount = 0L;
        Long usedPoint = request.usedPoint() == null ? 0L : request.usedPoint();
        Long finalAmount = totalAmount - discountAmount - usedPoint;

        if (finalAmount < 0) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_INVALID_AMOUNT);
        }

        Order order = Order.create(
                request.memberId(),
                request.memberCouponId(),
                generateOrderNum(),
                usedPoint,
                totalAmount,
                discountAmount,
                finalAmount,
                request.memberAddressSnap(),
                request.memberAddressDetailSnap()
        );

        for (OrderCreateRequest.OrderItemRequest item : request.orderItems()) {
            OrderDetail detail = OrderDetail.create(
                    item.productId(),
                    item.productNameSnap(),
                    item.productPriceSnap(),
                    item.quantity()
            );
            order.addOrderDetail(detail);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderCreateResponse.from(savedOrder);
    }

    private Long calculateTotalAmount(OrderCreateRequest request) {
        return request.orderItems().stream()
                .mapToLong(item -> item.productPriceSnap() * item.quantity())
                .sum();
    }

    private String generateOrderNum() {
        return "order_" + UUID.randomUUID();
    }
}
