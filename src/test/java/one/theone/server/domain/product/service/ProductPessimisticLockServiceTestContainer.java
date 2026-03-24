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

import static org.assertj.core.api.Assertions.assertThat;
import one.theone.server.common.RedisTestContainer;

public class ProductPessimisticLockServiceTestContainer extends RedisTestContainer {
    
    


    @Autowired
    private ProductService productService;

    @Autowired
    private ProductPessimisticLockService productPessimisticLockService;

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
    @DisplayName("withPessimisticLock - decreaseStock")
    void withPessimisticLock_decreaseStock() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productPessimisticLockService.decreaseStock(productId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(0L);
        System.out.println("비관적 락 최종 재고 : " + product.getQuantity());
    }

    @Test
    @DisplayName("withPessimisticLock - increaseStock")
    void withPessimisticLock_increaseStock() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productPessimisticLockService.increaseStock(productId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(200L);
        System.out.println("비관적 락 최종 재고 : " + product.getQuantity());
    }
}
