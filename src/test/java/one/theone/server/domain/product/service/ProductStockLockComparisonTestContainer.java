package one.theone.server.domain.product.service;

import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import one.theone.server.common.RedisTestContainer;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductStockLockComparisonTestContainer extends RedisTestContainer {




    @Autowired
    private ProductService productService;

    @Autowired
    private ProductPessimisticLockService productPessimisticLockService;

    @Autowired
    private ProductOptimisticLockService productOptimisticLockService;

    @Autowired
    private ProductStockService productStockService;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void beforeSetUp() {
        // 재고 100개 상품 생성
        Product product = productRepository.save(Product.register("test", 1000L, BigDecimal.valueOf(10L), 1000, 1L, 100L));
        productId = product.getId();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("withoutLock")
    void withoutLock_decreaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStock(productId, 1L);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long time = System.currentTimeMillis() - start;

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isNotEqualTo(0);
        System.out.printf("[락 없음] 소요 시간: %dms, 최종 재고: %d, 실패: %d%n", time, product.getQuantity(), failCount.get());
    }

    @Test
    @Order(2)
    @DisplayName("withPessimisticLock")
    void withPessimisticLock_decreaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productPessimisticLockService.decreaseStock(productId, 1L);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long time = System.currentTimeMillis() - start;

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(failCount.get());
        System.out.printf("[비관적락] 소요 시간: %dms, 최종 재고: %d, 실패: %d%n", time, product.getQuantity(), failCount.get());
    }

    @Test
    @Order(3)
    @DisplayName("withOptimisticLock")
    void withOptimisticLock_decreaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productOptimisticLockService.decreaseStock(productId, 1L);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long time = System.currentTimeMillis() - start;

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(failCount.get());
        System.out.printf("[낙관적락] 소요 시간: %dms, 최종 재고: %d, 실패: %d%n", time, product.getQuantity(), failCount.get());
    }

    @Test
    @Order(4)
    @DisplayName("WithRedisLock")
    void withSpinLock_decreaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productStockService.decreaseStock(productId, 1L);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long time = System.currentTimeMillis() - start;

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(failCount.get());
        System.out.printf("[분산락] 소요 시간: %dms, 최종 재고: %d, 실패: %d%n", time, product.getQuantity(), failCount.get());
    }
}
