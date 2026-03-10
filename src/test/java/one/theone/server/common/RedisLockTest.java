package one.theone.server.common;

// 테스트 코드 -> 현재는 미작동
// 스프링 컨텍스트가 있어야 테스트가 가능한 AOP를 사용 했으므로 SpringBootTest 사용해야함

import com.redis.testcontainers.RedisContainer;
import one.theone.server.domain.product.entity.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/*
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class RedisLockTest {
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

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void beforeSetUp() {
        // 재고 100개 상품 생성
        Product product = productRepository.save(Product.register("test", 100, 1000L));
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
                    productService.decreaseStockWithNoLock(productId, 1);
                } finally {
                    latch.countDown(); // 스레드 완료 카운트 감소
                }
            });
        }

        latch.await(); // 모든 스레드 완료까지 대기
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        // 동시성 문제로 재고가 0이 아님
        assertThat(product.getQuantity()).isNotEqualTo(0);
        System.out.println("락 없는 최종 재고 : " + product.getQuantity());
    }

    @Test
    @DisplayName("WithRedisLock")
    void withSpinLock() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    productService.decreaseStock(productId, 1);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Product product = productRepository.findById(productId).orElseThrow();

        assertThat(product.getQuantity()).isEqualTo(0);
        System.out.println("레디스 락 최종 재고 : " + product.getQuantity());
    }
}
*/

