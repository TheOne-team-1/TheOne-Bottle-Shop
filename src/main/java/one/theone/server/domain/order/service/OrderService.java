package one.theone.server.domain.order.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CartExceptionEnum;
import one.theone.server.common.exception.domain.OrderExceptionEnum;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.dto.response.OrderCreateResponse;
import one.theone.server.domain.order.dto.response.OrderDetailGetResponse;
import one.theone.server.domain.order.dto.response.OrderListGetResponse;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderQueryRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
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
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderQueryRepository orderQueryRepository;

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

        if (cartEntries.isEmpty()) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_CART_ITEM_EMPTY);
        }

        List<Long> productIds = cartEntries.keySet().stream()
                .map(key -> Long.valueOf(key.toString()))
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND);
        }

        Long totalAmount = calculateTotalAmountFromCart(products, cartEntries);
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

        for (Product product : products) {
            Object quantityValue = cartEntries.get(product.getId().toString());

            if (quantityValue == null) {
                continue;
            }

            Integer quantity = Integer.valueOf(quantityValue.toString());

            OrderDetail detail = (OrderDetail.create(
                    savedOrder.getId(),
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantity
            ));

            details.add(detail);
        }

        orderDetailRepository.saveAll(details);
        redisTemplate.delete(cartKey);

        return OrderCreateResponse.from(savedOrder);

    }

    @Transactional(readOnly = true)
    public List<OrderListGetResponse> getOrderList(Long memeberId) {
        return orderQueryRepository.findOrderListByMemberId(memeberId);
    }

    @Transactional(readOnly = true)
    public OrderDetailGetResponse getOrderDetail(Long memberId, Long orderId) {
        return orderQueryRepository.findOrderDetailByOrderIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_NOT_FOUND));
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

    private Long calculateTotalAmountFromCart(List<Product> products, Map<Object, Object> cartEntries) {
        long totalAmount = 0L;

        for (Product product : products) {
            Object quantityValue = cartEntries.get(product.getId().toString());

            if (quantityValue == null) {
                continue;
            }

            Integer quantity = Integer.valueOf(quantityValue.toString());
            totalAmount += product.getPrice() * quantity;
        }

        return totalAmount;
    }

    private String generateOrderNum() {
        return "order_" + UUID.randomUUID();
    }

    private String generateCartKey(Long memberId) {
        return "cart:member:" + memberId;
    }
}
