package one.theone.server.domain.order.service;

import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.dto.response.OrderCancelResponse;
import one.theone.server.domain.order.dto.response.OrderCreateResponse;
import one.theone.server.domain.order.dto.response.OrderDetailGetResponse;
import one.theone.server.domain.order.dto.response.OrderListGetResponse;
import one.theone.server.domain.order.dto.response.OrderPageResponse;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.entity.OrderDetail;
import one.theone.server.domain.order.entity.OrderStatus;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderQueryRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private OrderQueryRepository orderQueryRepository;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OrderService orderService;


    private Product createProduct(Long id, String name, Long price, Long quantity) {
        Product product = Product.register(
                name,
                price,
                BigDecimal.valueOf(13.0),
                750,
                1L,
                quantity
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Order createOrder(Long id, Long memberId, String orderNum) {
        Order order = Order.create(
                memberId,
                null,
                orderNum,
                0L,
                10000L,
                0L,
                10000L,
                "서울시",
                "101동"
        );
        ReflectionTestUtils.setField(order, "id", id);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2026, 3, 18, 10, 0));
        return order;
    }

    @Nested
    class CreateDirectOrderTest {

        @Test
        @DisplayName("바로구매 주문 생성에 성공한다")
        void createDirectOrder_success() {
            Product product = createProduct(10L, "와인", 10000L, 100L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment(anyString())).willReturn(1L);

            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                    10L,
                    2,
                    null,
                    0L,
                    "서울시 강남구",
                    "101동 1001호"
            );

            OrderCreateResponse response = orderService.createDirectOrder(1L, request);

            then(orderDetailRepository).should().save(any(OrderDetail.class));
            assertThat(response.orderId()).isEqualTo(1L);
            assertThat(response.totalAmount()).isEqualTo(20000L);
            assertThat(response.finalAmount()).isEqualTo(20000L);
            assertThat(response.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        }

        @Test
        @DisplayName("주문번호는 yyyyMMdd-00000001 형식으로 생성된다")
        void createDirectOrder_orderNumFormat() {
            Product product = createProduct(10L, "와인", 10000L, 100L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment(anyString())).willReturn(1L);

            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                    10L, 1, null, 0L, "서울시", "101동"
            );

            orderService.createDirectOrder(1L, request);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            then(orderRepository).should().save(captor.capture());

            assertThat(captor.getValue().getOrderNum())
                    .matches(Pattern.compile("\\d{8}-\\d{8}"));
        }

        @Test
        @DisplayName("상품이 없으면 실패한다")
        void createDirectOrder_fail_productNotFound() {
            given(productRepository.findById(10L)).willReturn(Optional.empty());

            OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                    10L, 1, null, 0L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createDirectOrder(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("수량이 잘못되면 실패한다")
        void createDirectOrder_fail_invalidQuantity() {
            Product product = createProduct(10L, "와인", 10000L, 100L);
            given(productRepository.findById(10L)).willReturn(Optional.of(product));

            OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                    10L, 0, null, 0L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createDirectOrder(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("재고를 초과하면 실패한다")
        void createDirectOrder_fail_stockExceeded() {
            Product product = createProduct(10L, "와인", 10000L, 1L);
            given(productRepository.findById(10L)).willReturn(Optional.of(product));

            OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                    10L, 2, null, 0L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createDirectOrder(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("최종 금액이 0 미만이면 실패한다")
        void createDirectOrder_fail_invalidAmount() {
            Product product = createProduct(10L, "와인", 10000L, 100L);
            given(productRepository.findById(10L)).willReturn(Optional.of(product));

            OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                    10L, 1, null, 20000L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createDirectOrder(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("주문번호 생성 카운트가 null이면 실패한다")
        void createDirectOrder_fail_orderNumGeneration() {
            Product product = createProduct(10L, "와인", 10000L, 100L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment(anyString())).willReturn(null);

            OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                    10L, 1, null, 0L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createDirectOrder(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }
    }

    @Nested
    class CreateOrderFromCartTest {

        @Test
        @DisplayName("장바구니가 비어 있으면 실패한다")
        void createOrderFromCart_fail_emptyCart() {
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries("cart:member:1")).willReturn(Map.of());

            OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                    null, 0L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("장바구니 주문 생성에 성공하면 상세를 저장하고 장바구니를 비운다")
        void createOrderFromCart_success() {
            String cartKey = "cart:member:1";
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(cartKey)).willReturn(Map.of("10", "2"));

            Product product = createProduct(10L, "와인", 10000L, 100L);
            given(productRepository.findAllById(List.of(10L))).willReturn(List.of(product));

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.increment(anyString())).willReturn(1L);

            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                ReflectionTestUtils.setField(order, "id", 1L);
                return order;
            });

            OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                    null, 0L, "서울시", "101동"
            );

            OrderCreateResponse response = orderService.createOrderFromCart(1L, request);

            then(orderDetailRepository).should().saveAll(anyList());
            then(redisTemplate).should().delete(cartKey);
            assertThat(response.orderId()).isEqualTo(1L);
            assertThat(response.totalAmount()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("stale item이 섞여 있으면 삭제 후 실패한다")
        void createOrderFromCart_fail_staleItem() {
            String cartKey = "cart:member:1";
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(cartKey)).willReturn(Map.of("10", "2", "999", "1"));

            Product product = createProduct(10L, "와인", 10000L, 100L);
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                    null, 0L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, request))
                    .isInstanceOf(ServiceErrorException.class);

            then(hashOperations).should().delete(eq(cartKey), any(Object[].class));
        }

        @Test
        @DisplayName("장바구니 상품 재고가 부족하면 실패한다")
        void createOrderFromCart_fail_stockExceeded() {
            String cartKey = "cart:member:1";
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(cartKey)).willReturn(Map.of("10", "2"));

            Product product = createProduct(10L, "와인", 10000L, 1L);
            given(productRepository.findAllById(List.of(10L))).willReturn(List.of(product));

            OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                    null, 0L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("최종 금액이 0 미만이면 실패한다")
        void createOrderFromCart_fail_invalidAmount() {
            String cartKey = "cart:member:1";
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(cartKey)).willReturn(Map.of("10", "1"));

            Product product = createProduct(10L, "와인", 10000L, 100L);
            given(productRepository.findAllById(List.of(10L))).willReturn(List.of(product));

            OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                    null, 20000L, "서울시", "101동"
            );

            assertThatThrownBy(() -> orderService.createOrderFromCart(1L, request))
                    .isInstanceOf(ServiceErrorException.class);
        }
    }

    @Nested
    class ReadAndCancelTest {

        @Test
        @DisplayName("주문 목록 조회에 성공한다")
        void getOrderList_success() {
            Order order = createOrder(1L, 1L, "20260318-00000001");
            Page<Order> page = new PageImpl<>(List.of(order));

            given(orderRepository.findByMemberIdOrderByCreatedAtDesc(eq(1L), any()))
                    .willReturn(page);

            OrderPageResponse response = orderService.getOrderList(1L, 0, 10);

            assertThat(response.content()).hasSize(1);
            Object content = response.content().get(0);
            assertThat(content).isInstanceOf(OrderListGetResponse.class);
        }

        @Test
        @DisplayName("주문 상세 조회에 성공한다")
        void getOrderDetail_success() {
            OrderDetailGetResponse detail = new OrderDetailGetResponse(
                    1L,
                    "20260318-00000001",
                    OrderStatus.PENDING_PAYMENT,
                    0L,
                    10000L,
                    0L,
                    10000L,
                    "서울시",
                    "101동",
                    LocalDateTime.now(),
                    List.of()
            );

            given(orderQueryRepository.findOrderDetail(1L, 1L)).willReturn(Optional.of(detail));

            OrderDetailGetResponse response = orderService.getOrderDetail(1L, 1L);

            assertThat(response.orderId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("주문 상세가 없으면 실패한다")
        void getOrderDetail_fail_notFound() {
            given(orderQueryRepository.findOrderDetail(1L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderDetail(1L, 1L))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("주문 취소에 성공한다")
        void cancelOrder_success() {
            Order order = createOrder(1L, 1L, "20260318-00000001");
            given(orderRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(order));

            OrderCancelResponse response = orderService.cancelOrder(1L, 1L);

            assertThat(response.orderId()).isEqualTo(1L);
            assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("주문이 없으면 취소 실패")
        void cancelOrder_fail_notFound() {
            given(orderRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("이미 취소된 주문이면 실패한다")
        void cancelOrder_fail_alreadyCancelled() {
            Order order = createOrder(1L, 1L, "20260318-00000001");
            order.markCancelled();

            given(orderRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("결제 대기 상태가 아니면 취소 실패")
        void cancelOrder_fail_notAllowed() {
            Order order = createOrder(1L, 1L, "20260318-00000001");
            order.markCompleted();

            given(orderRepository.findByIdAndMemberId(1L, 1L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                    .isInstanceOf(ServiceErrorException.class);
        }
    }
}