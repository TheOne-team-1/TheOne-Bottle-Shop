package one.theone.server.domain.order.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.annotation.RedissonLock;
import one.theone.server.common.config.cache.CacheConfig;
import one.theone.server.common.config.redis.RedisLockService;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CartExceptionEnum;
import one.theone.server.common.exception.domain.OrderExceptionEnum;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.dto.response.*;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderQueryRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderQueryRepository orderQueryRepository;
    private final RedisLockService redisLockService;

    private static final long LOCK_WAIT_TIME = 1L;
    private static final long LOCK_LEASE_TIME = 1L;

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ORDER_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ORDER_DETAIL_CACHE, allEntries = true)
    })
    @Transactional
    public OrderCreateResponse createDirectOrder(Long memberId, OrderCreateDirectRequest request) {
        String lockKey = "lock:order:direct:" + memberId + ":" + request.productId();
        return executeWithLock(lockKey, () -> createDirectOrderInternal(memberId, request));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ORDER_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ORDER_DETAIL_CACHE, allEntries = true)
    })
    @RedissonLock(key = "'order:direct:' + #memberId + ':' + #request.productId()")
    public OrderCreateResponse createDirectOrderWithRedisson(Long memberId, OrderCreateDirectRequest request) {
        return createDirectOrderInternal(memberId, request);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ORDER_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ORDER_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CART_CACHE, key = "'member:' + #memberId")
    })
    @Transactional
    public OrderCreateResponse createOrderFromCart(Long memberId, OrderCreateFromCartRequest request) {
        String lockKey = "lock:order:cart:" + memberId;
        return executeWithLock(lockKey, () -> createOrderFromCartInternal(memberId, request));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ORDER_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ORDER_DETAIL_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.CART_CACHE, key = "'member:' + #memberId")
    })
    @RedissonLock(key = "'order:cart:' + #memberId")
    public OrderCreateResponse createOrderFromCartWithRedisson(Long memberId, OrderCreateFromCartRequest request) {
        return createOrderFromCartInternal(memberId, request);
    }

    @Cacheable(
            value = CacheConfig.ORDER_LIST_CACHE,
            key = "'member:' + #memberId + ':page:' + #page + ':size:' + #pageSize"
    )
    @Transactional(readOnly = true)
    public OrderPageResponse getOrderList(Long memberId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Order> orderPage = orderRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        List<OrderListGetResponse> content = orderPage.getContent().stream()
                .map(OrderListGetResponse::from)
                .toList();

        return new OrderPageResponse(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast()
        );
    }

    @Cacheable(
            value = CacheConfig.ORDER_DETAIL_CACHE,
            key = "'member:' + #memberId + ':order:' + #orderId"
    )
    @Transactional(readOnly = true)
    public OrderDetailGetResponse getOrderDetail(Long memberId, Long orderId) {
        return orderQueryRepository.findOrderDetail(orderId, memberId)
                .orElseThrow(() -> new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_NOT_FOUND));
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.ORDER_LIST_CACHE, allEntries = true),
            @CacheEvict(value = CacheConfig.ORDER_DETAIL_CACHE, allEntries = true)
    })
    @Transactional
    public OrderCancelResponse cancelOrder(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_ALREADY_CANCELLED);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_CANCEL_NOT_ALLOWED);
        }

        order.markCancelled();

        return new OrderCancelResponse(
                order.getId(),
                order.getStatus()
        );
    }

    private OrderCreateResponse createDirectOrderInternal(Long memberId, OrderCreateDirectRequest request) {
        Product product = productRepository.findById(request.productId()).orElseThrow(
                () -> new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND)
        );

        if (request.quantity() == null || request.quantity() < 1) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_INVALID_QUANTITY);
        }

        validateOrderStock(product.getQuantity(), request.quantity());

        Long totalAmount = product.getPrice() * request.quantity();
        Long discountAmount = 0L;
        Long usedPoint = request.usedPoint() == null ? 0L : request.usedPoint();
        Long finalAmount = totalAmount - discountAmount - usedPoint;

        if (finalAmount < 0) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_INVALID_AMOUNT);
        }

        Order order = Order.create(
                memberId,
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

        OrderDetail detail =  OrderDetail.create(
                savedOrder.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                request.quantity()
        );

        orderDetailRepository.save(detail);

        return OrderCreateResponse.from(savedOrder);
    }

    private OrderCreateResponse createOrderFromCartInternal(Long memberId, OrderCreateFromCartRequest request) {
        String cartKey = generateCartKey(memberId);

        Map<Object, Object> cartEntries = redisTemplate.opsForHash().entries(cartKey);

        if (cartEntries.isEmpty()) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_CART_ITEM_EMPTY);
        }

        List<Long> productIds = cartEntries.keySet().stream()
                .map(key -> Long.valueOf(key.toString()))
                .toList();

        List<Product> products = productRepository.findAllById(productIds);
        removeStaleCartItems(cartKey, productIds, products);

        if (products.size() != productIds.size()) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_ITEM_NOT_FOUND);
        }

        Long totalAmount = calculateTotalAmountFromCart(products, cartEntries);
        Long discountAmount = 0L;
        Long usedPoint = request.usedPoint() == null ? 0L : request.usedPoint();
        Long finalAmount = totalAmount - discountAmount - usedPoint;

        if (finalAmount < 0) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_INVALID_AMOUNT);
        }

        Order order = Order.create(
                memberId,
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

            details.add(OrderDetail.create(
                    savedOrder.getId(),
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantity
            ));
        }

        if (details.isEmpty()) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_ITEM_NOT_FOUND);
        }

        orderDetailRepository.saveAll(details);
        redisTemplate.delete(cartKey);

        return OrderCreateResponse.from(savedOrder);
    }

    private <T> T executeWithLock(String lockKey, LockAction<T> action) {
        String lockValue = null;
        try {
            lockValue = redisLockService.tryLock(
                    lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (lockValue == null) {
                throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_REQUEST_CONFLICT);
            }
            return action.execute();
        } catch (InterruptedException e) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_REQUEST_CONFLICT);
        } finally {
            if (lockValue != null) {
                redisLockService.unLock(lockKey, lockValue);
            }
        }
    }

    @FunctionalInterface
    private interface LockAction<T> {
        T execute();
    }

    private Long calculateTotalAmountFromCart(List<Product> products, Map<Object, Object> cartEntries) {
        long totalAmount = 0L;

        for (Product product : products) {
            Object quantityValue = cartEntries.get(product.getId().toString());

            if (quantityValue == null) {
                continue;
            }

            Integer quantity = Integer.valueOf(quantityValue.toString());

            validateOrderStock(product.getQuantity(), quantity.longValue());

            totalAmount += product.getPrice() * quantity;
        }

        return totalAmount;
    }

    private String generateOrderNum() {

        String datePrefix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        String redisKey = "order:count:" + datePrefix;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count == null) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_CREATE_FAILED);
        }

        redisTemplate.expire(redisKey, java.time.Duration.ofDays(1));
        return String.format("%s-%08d", datePrefix, count);
    }

    private String generateCartKey(Long memberId) {
        return "cart:member:" + memberId;
    }

    private void validateOrderStock(Long stockQuantity, long requestedQuantity) {
        if (stockQuantity == null || stockQuantity < requestedQuantity) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_STOCK_EXCEEDED);
        }
    }

    private void removeStaleCartItems(String cartKey, List<Long> productIds, List<Product> products) {
        Set<Long> foundIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toSet());

        List<Object> staleFields = productIds.stream()
                .filter(productId -> !foundIds.contains(productId))
                .map(String::valueOf)
                .map(field -> (Object) field)
                .toList();

        if (!staleFields.isEmpty()) {
            redisTemplate.opsForHash().delete(cartKey, staleFields.toArray());
        }
    }
}
