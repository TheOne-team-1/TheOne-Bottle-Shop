package one.theone.server.domain.order.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.OrderExceptionEnum;
import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.dto.response.OrderCreateResponse;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional
    public OrderCreateResponse createDirectOrder(OrderCreateDirectRequest request) {
        validateCreateOrderRequest(request);

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

        Order savedOrder = orderRepository.save(order);

        List<OrderDetail> details = new ArrayList<>();

        for (OrderCreateDirectRequest.OrderItemRequest item : request.orderItems()) {
            details.add(OrderDetail.create(
                    savedOrder.getId(),
                    item.productId(),
                    item.productNameSnap(),
                    item.productPriceSnap(),
                    item.quantity()
            ));
        }

        orderDetailRepository.saveAll(details);

        return OrderCreateResponse.from(savedOrder);
    }

    @Transactional
    public OrderCreateResponse createOrderFromCart(OrderCreateFromCartRequest request) {
        String cartKey = generateCartKey(request.memberId());

        Map<Object, Object> cartEntries = redisTemplate.opsForHash().entries(cartKey);

    }

    private void validateCreateOrderRequest(OrderCreateDirectRequest request) {
        if (request.orderItems() == null || request.orderItems().isEmpty()) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_ITEM_EMPTY);
        }

        for (OrderCreateDirectRequest.OrderItemRequest item : request.orderItems()) {
            if (item.quantity() == null || item.quantity() < 1) {
                throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_INVALID_QUANTITY);
            }

            if (item.productPriceSnap() == null || item.productPriceSnap() < 0) {
                throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_INVALID_PRICE);
            }
        }
    }

    private Long calculateTotalAmount(OrderCreateDirectRequest request) {
        return request.orderItems().stream()
                .mapToLong(item -> item.productPriceSnap() * item.quantity())
                .sum();
    }

    private String generateOrderNum() {
        return "order_" + UUID.randomUUID();
    }

    private String generateCartKey(Long memberId) {
        return "cart:member:" + memberId;
    }
}
