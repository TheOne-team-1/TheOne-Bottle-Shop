package one.theone.server.domain.cart.service;

import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.cart.dto.request.CartAddRequest;
import one.theone.server.domain.cart.dto.request.CartUpdateQuantityRequest;
import one.theone.server.domain.cart.dto.response.CartAddResponse;
import one.theone.server.domain.cart.dto.response.CartRemoveItemResponse;
import one.theone.server.domain.cart.dto.response.CartRemoveResponse;
import one.theone.server.domain.cart.dto.response.CartResponse;
import one.theone.server.domain.cart.dto.response.CartUpdateQuantityResponse;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private CartService cartService;

    private Product createProduct(Long id, String name, Long price, Long quantity) {
        Product product = Product.register(
                name,
                price,
                BigDecimal.valueOf(12.5),
                750,
                1L,
                quantity
        );
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    @Nested
    class AddItemTest {

        @Test
        @DisplayName("장바구니에 처음 상품을 담으면 수량이 저장된다")
        void addItem_success_firstAdd() {
            Long memberId = 1L;
            String cartKey = "cart:member:" + memberId;
            String field = "10";

            Product product = createProduct(10L, "와인", 10000L, 10L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.get(cartKey, field)).willReturn(null);
            given(hashOperations.get(cartKey, field)).willReturn("2");

            CartAddResponse response = cartService.addItem(memberId, new CartAddRequest(10L, 2));

            then(hashOperations).should().increment(cartKey, field, 2);
            assertThat(response.productId()).isEqualTo(10L);
            assertThat(response.quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("같은 상품을 다시 담으면 기존 수량에 누적된다")
        void addItem_success_accumulateQuantity() {
            Long memberId = 1L;
            String cartKey = "cart:member:" + memberId;
            String field = "10";

            Product product = createProduct(10L, "와인", 10000L, 10L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.get(cartKey, field)).willReturn("2");
            given(hashOperations.get(cartKey, field)).willReturn("5");

            CartAddResponse response = cartService.addItem(memberId, new CartAddRequest(10L, 3));

            then(hashOperations).should().increment(cartKey, field, 3);
            assertThat(response.quantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("상품 ID가 없으면 실패한다")
        void addItem_fail_invalidProductId() {
            assertThatThrownBy(() -> cartService.addItem(1L, new CartAddRequest(null, 1)))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("수량이 1 미만이면 실패한다")
        void addItem_fail_invalidQuantity() {
            assertThatThrownBy(() -> cartService.addItem(1L, new CartAddRequest(10L, 0)))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("상품이 없으면 실패한다")
        void addItem_fail_productNotFound() {
            given(productRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addItem(1L, new CartAddRequest(10L, 1)))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("재고를 초과하면 실패한다")
        void addItem_fail_stockExceeded() {
            Product product = createProduct(10L, "와인", 10000L, 3L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.get("cart:member:1", "10")).willReturn("2");

            assertThatThrownBy(() -> cartService.addItem(1L, new CartAddRequest(10L, 2)))
                    .isInstanceOf(ServiceErrorException.class);
        }
    }

    @Nested
    class GetCartTest {

        @Test
        @DisplayName("장바구니가 비어 있으면 빈 응답을 반환한다")
        void getCart_empty() {
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries("cart:member:1")).willReturn(Map.of());

            CartResponse response = cartService.getCart(1L);

            assertThat(response.items()).isEmpty();
            assertThat(response.totalAmount()).isZero();
        }

        @Test
        @DisplayName("장바구니 조회 시 존재하지 않는 stale item은 Redis에서 제거한다")
        void getCart_removeStaleItem() {
            Long memberId = 1L;
            String cartKey = "cart:member:" + memberId;

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(cartKey)).willReturn(Map.of("10", "2", "999", "1"));

            Product product = createProduct(10L, "와인", 10000L, 50L);
            given(productRepository.findAllById(any())).willReturn(List.of(product));

            CartResponse response = cartService.getCart(memberId);

            then(hashOperations).should().delete(eq(cartKey), any(Object[].class));
            assertThat(response.items()).hasSize(1);
            assertThat(response.totalAmount()).isEqualTo(20000L);
        }

        @Test
        @DisplayName("정상 상품만 있으면 합계 금액을 계산한다")
        void getCart_success() {
            Long memberId = 1L;
            String cartKey = "cart:member:" + memberId;

            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(cartKey)).willReturn(Map.of("10", "2", "11", "1"));

            Product wine = createProduct(10L, "와인", 10000L, 50L);
            Product beer = createProduct(11L, "맥주", 5000L, 20L);

            given(productRepository.findAllById(any())).willReturn(List.of(wine, beer));

            CartResponse response = cartService.getCart(memberId);

            assertThat(response.items()).hasSize(2);
            assertThat(response.totalAmount()).isEqualTo(25000L);
        }
    }

    @Nested
    class UpdateQuantityTest {

        @Test
        @DisplayName("수량 변경에 성공한다")
        void updateQuantity_success() {
            Product product = createProduct(10L, "와인", 10000L, 10L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:member:1", "10")).willReturn(true);

            CartUpdateQuantityResponse response =
                    cartService.updateQuantity(1L, 10L, new CartUpdateQuantityRequest(3));

            then(hashOperations).should().put("cart:member:1", "10", 3);
            assertThat(response.productId()).isEqualTo(10L);
            assertThat(response.quantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("productId가 잘못되면 실패한다")
        void updateQuantity_fail_invalidProductId() {
            assertThatThrownBy(() ->
                    cartService.updateQuantity(1L, 0L, new CartUpdateQuantityRequest(1)))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("수량이 잘못되면 실패한다")
        void updateQuantity_fail_invalidQuantity() {
            assertThatThrownBy(() ->
                    cartService.updateQuantity(1L, 10L, new CartUpdateQuantityRequest(0)))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("상품이 없으면 실패한다")
        void updateQuantity_fail_productNotFound() {
            given(productRepository.findById(10L)).willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    cartService.updateQuantity(1L, 10L, new CartUpdateQuantityRequest(1)))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("장바구니에 상품이 없으면 실패한다")
        void updateQuantity_fail_cartItemNotFound() {
            Product product = createProduct(10L, "와인", 10000L, 10L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:member:1", "10")).willReturn(false);

            assertThatThrownBy(() ->
                    cartService.updateQuantity(1L, 10L, new CartUpdateQuantityRequest(3)))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("재고 초과 수량으로 변경하면 실패한다")
        void updateQuantity_fail_stockExceeded() {
            Product product = createProduct(10L, "와인", 10000L, 2L);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));

            assertThatThrownBy(() ->
                    cartService.updateQuantity(1L, 10L, new CartUpdateQuantityRequest(3)))
                    .isInstanceOf(ServiceErrorException.class);
        }
    }

    @Nested
    class RemoveTest {

        @Test
        @DisplayName("장바구니 상품 삭제에 성공한다")
        void removeItem_success() {
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:member:1", "10")).willReturn(true);

            CartRemoveItemResponse response = cartService.removeItem(1L, 10L);

            then(hashOperations).should().delete("cart:member:1", "10");
            assertThat(response.productId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("삭제할 상품이 장바구니에 없으면 실패한다")
        void removeItem_fail_notFound() {
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.hasKey("cart:member:1", "10")).willReturn(false);

            assertThatThrownBy(() -> cartService.removeItem(1L, 10L))
                    .isInstanceOf(ServiceErrorException.class);
        }

        @Test
        @DisplayName("장바구니 전체 비우기에 성공한다")
        void removeCart_success() {
            CartRemoveResponse response = cartService.removeCart(1L);

            then(redisTemplate).should().delete("cart:member:1");
            assertThat(response.message()).isEqualTo("장바구니가 비워졌습니다");
        }
    }
}