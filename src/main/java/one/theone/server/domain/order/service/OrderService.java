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
        BigDecimal totalAmount = calculateTotalAmount(request);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal usedPoint = defaultIfNull(request.usedPoint());
        BigDecimal finalAmount = totalAmount.subtract(discountAmount).subtract(usedPoint);

        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
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

    private BigDecimal calculateTotalAmount(OrderCreateRequest request) {
        return request.orderItems().stream()
                .map(item -> item.productPriceSnap().multiply(
                        BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal defaultIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String generateOrderNum() {
        return "order_" + UUID.randomUUID();
    }
}
