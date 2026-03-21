package one.theone.server.domain.product.service;

import com.redis.testcontainers.RedisContainer;
import one.theone.server.domain.point.event.PointEarnPublisher;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class ProductRedissonTest {
    // withExposedPorts(6379) : 랜덤 포트로 Redis 실행
    @Container
    static final RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:8.6.1")).withExposedPorts(6379);

    // @DynamicPropertySource
    // Docker가 랜덤 포트로 Redis를 실행하기 때문에 실행 후 포트를 Spring 설정에 주입
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redisContainer.getHost());
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @MockitoBean
    private KomoranCorrector komoranCorrector;  // 실제 인스턴스화 차단

    @MockitoBean
    private PointEarnPublisher pointEarnPublisher;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void beforeSetUp() {
        // 재고 100개 상품 생성
        Product product = productRepository.save(Product.register("test", 1000L, BigDecimal.valueOf(13.5), 1000, 1L, 100L));
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
                    // 락 없는 재고 감소
                    productService.decreaseStock(productId, 1L);
                } finally {
                    latch.countDown(); // 스레드 완료 카운트 감소
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        // 동시성 문제로 재고가 0이 아님
        System.out.println("락 없는 최종 재고 : " + product.getQuantity());
        assertThat(product.getQuantity()).isNotEqualTo(0);
    }

    @Test
    @DisplayName("WithRedissonLock - decreaseStock")
    void withRedisson_decreaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStockWithRedisson(productId, 1L);
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

        System.out.println("Redisson 락 최종 재고 : " + product.getQuantity());
        assertThat(product.getQuantity()).isEqualTo(failCount.get());
    }

    @Test
    @DisplayName("WithRedissonLock - increaseStock")
    void withRedisson_increaseStock() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.increaseStockWithRedisson(productId, 1L);
                } catch (Exception e) {
                    System.out.println("error: " + e.getClass().getName() + " - " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        System.out.println("Redisson 락 최종 재고 : " + product.getQuantity());
        System.out.println("Redisson 락 failCount : " + failCount.get());
        assertThat(product.getQuantity()).isEqualTo(200L - failCount.get());
    }
}
