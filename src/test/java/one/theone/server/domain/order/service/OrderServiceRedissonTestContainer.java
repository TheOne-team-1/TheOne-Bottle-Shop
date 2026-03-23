package one.theone.server.domain.order.service;

import one.theone.server.domain.order.dto.request.OrderCreateDirectRequest;
import one.theone.server.domain.order.dto.request.OrderCreateFromCartRequest;
import one.theone.server.domain.order.repository.OrderDetailRepository;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import one.theone.server.common.RedisTestContainer;

public class OrderServiceRedissonTestContainer extends RedisTestContainer {





    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private Long productId;

    @BeforeEach
    void setUp() {
        Product product = productRepository.save(
                Product.register("testWhiskey", 100000L, BigDecimal.valueOf(75.0), 750, 1L, 100L)
        );
        productId = product.getId();
    }

    @AfterEach
    void tearDown() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("WithRedissonLock - 동시 바로구매 요청")
    void createDirectOrderWithRedisson_concurrent() throws InterruptedException {
        int threadCount = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 1; i <= threadCount; i++) {
            final long memberId = i;
            executorService.submit(() -> {
                try {
                    OrderCreateDirectRequest request = new OrderCreateDirectRequest(
                            productId, 1, null, 0L, "testAddress1", "testAddress2"
                    );
                    orderService.createDirectOrderWithRedisson(memberId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        System.out.println("바로구매 동시 요청 - 성공 : " + successCount.get() + "/ 실패 : " + failCount.get());
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
    }


    @Test
    @DisplayName("Redisson - 동시 장바구니 주문에서 첫 번째 요청만 성공하고 나머지는 실패")
    void createOrderFromCartWithRedisson_concurrent_onlyOne() throws InterruptedException {
        // 장바구니 설정
        Long memberId = 2L;
        String cartKey = "cart:member:" + memberId;
        redisTemplate.opsForHash().put(cartKey, productId.toString(), 1);

        int threadCount = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    OrderCreateFromCartRequest request = new OrderCreateFromCartRequest(
                            null, 0L, "testAddress1", "testAddress2"
                    );
                    orderService.createOrderFromCartWithRedisson(memberId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        System.out.println("첫 주문만 - 성공 : " + successCount.get() + "/ 실패 : " + failCount.get());
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
    }
}
