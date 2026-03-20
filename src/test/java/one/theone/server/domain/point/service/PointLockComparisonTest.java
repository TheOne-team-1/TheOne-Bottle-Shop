package one.theone.server.domain.point.service;

import com.redis.testcontainers.RedisContainer;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.order.entity.Order;
import one.theone.server.domain.order.repository.OrderRepository;
import one.theone.server.domain.point.dto.PointAdjustRequest;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.entity.PointLog;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import one.theone.server.domain.point.repository.PointUseDetailRepository;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PointLockComparisonTest {

    @Container
    static final RedisContainer redisContainer = new RedisContainer(
            DockerImageName.parse("redis:8.6.1")).withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redisContainer.getHost());
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @MockitoBean
    private KomoranCorrector komoranCorrector;  // 실제 인스턴스화 차단

    @Autowired
    private PointService pointService;

    @Autowired
    private PointPessimisticLockService pointPessimisticLockService;

    @Autowired
    private PointOptimisticLockService pointOptimisticLockService;

    @Autowired
    private PointLockService pointLockService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PointUseDetailRepository pointUseDetailRepository;

    private Long memberId;
    private List<Long> orderIds = new ArrayList<>();

    @BeforeEach
    void beforeSetUp() {
        // 회원 생성
        String email = UUID.randomUUID() + "@test.com";
        String recommendCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Member member = memberRepository.save(
                Member.create(email, "password", "테스트", "20000101", recommendCode));
        memberId = member.getId();

        // 포인트 2000 세팅
        PointLog earnLog = PointLog.ofAdmin(memberId, new PointAdjustRequest(2000L, "테스트 지급"), 2000L);
        pointLogRepository.save(earnLog);
        Point point = Point.register(memberId);
        point.updateBalance(2000L);
        pointRepository.save(point);

        // 주문 20개 생성 (각 usedPoint = 100)
        for (int i = 0; i < 20; i++) {
            Order order = Order.create(
                    memberId, null, "ORDER-" + i,
                    100L,
                    10000L, 0L,
                    9900L, "주소", "상세주소");
            orderIds.add(orderRepository.save(order).getId());
        }
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        pointLogRepository.deleteAll();
        pointRepository.deleteAll();
        memberRepository.deleteAll();
        pointUseDetailRepository.deleteAll();
        orderIds.clear();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("withoutLock")
    void withoutLock_usePoint() throws InterruptedException {
        int threadCount = 20;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointService.usePoint(memberId, orderId);
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

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();

        assertThat(point.getBalance()).isNotEqualTo(0L);
        System.out.printf("[락 없음] 소요 시간: %dms, 최종 잔액: %d, 실패: %d%n", time, point.getBalance(), failCount.get());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("withPessimisticLock")
    void withPessimisticLock_usePoint() throws InterruptedException {
        int threadCount = 20;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointPessimisticLockService.usePoint(memberId, orderId);
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

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();

        assertThat(point.getBalance()).isEqualTo(failCount.get() * 100L);
        System.out.printf("[비관적락] 소요 시간: %dms, 최종 잔액: %d, 실패: %d%n", time, point.getBalance(), failCount.get());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("withOptimisticLock")
    void withOptimisticLock_usePoint() throws InterruptedException {
        int threadCount = 20;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointOptimisticLockService.usePoint(memberId, orderId);
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

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();

        assertThat(point.getBalance()).isEqualTo(failCount.get() * 100L);
        System.out.printf("[낙관적락] 소요 시간: %dms, 최종 잔액: %d, 실패: %d%n", time, point.getBalance(), failCount.get());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("withRedisLock")
    void withRedisLock_usePoint() throws InterruptedException {
        int threadCount = 20;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointLockService.usePoint(memberId, orderId);
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

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();

        assertThat(point.getBalance()).isEqualTo(failCount.get() * 100L);
        System.out.printf("[분산락] 소요 시간: %dms, 최종 잔액: %d, 실패: %d%n", time, point.getBalance(), failCount.get());
    }
}
