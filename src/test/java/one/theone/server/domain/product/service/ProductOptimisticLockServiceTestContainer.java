package one.theone.server.domain.product.service;

import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import one.theone.server.common.RedisTestContainer;

public class ProductOptimisticLockServiceTestContainer extends RedisTestContainer {
    
    


    @Autowired
    private ProductService productService;

    @Autowired
    private ProductOptimisticLockService productOptimisticLockService;

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
    @DisplayName("NO_LOCK")
    void withoutLock() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStock(productId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isNotEqualTo(0);
        System.out.println("락 없는 최종 재고 : " + product.getQuantity());
    }

    @Test
    @DisplayName("withOptimisticLock - decreaseStock")
    void withOptimisticLock_decreaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

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

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(failCount.get());
        System.out.println("낙관적 락 최종 재고 : " + product.getQuantity());
    }

    @Test
    @DisplayName("withOptimisticLock - increaseStock")
    void withOptimisticLock_increaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productOptimisticLockService.increaseStock(productId, 1L);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(200L - failCount.get());
        System.out.println("낙관적 락 최종 재고 : " + product.getQuantity());
    }
}
