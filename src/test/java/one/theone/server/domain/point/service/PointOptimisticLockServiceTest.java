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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
public class PointOptimisticLockServiceTest {

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
    private PointOptimisticLockService pointOptimisticLockService;

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

        // 포인트 10000 세팅
        PointLog earnLog = PointLog.ofAdmin(memberId, new PointAdjustRequest(10000L, "테스트 지급"), 10000L);
        pointLogRepository.save(earnLog);
        Point point = Point.register(memberId);
        point.updateBalance(10000L);
        pointRepository.save(point);

        // 주문 100개 생성 (각 usedPoint = 100)
        for (int i = 0; i < 100; i++) {
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
    @DisplayName("NO_LOCK - usePoint")
    void withoutLock_use() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointService.usePoint(memberId, orderId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();

        assertThat(point.getBalance()).isNotEqualTo(0);
        System.out.println("락 없는 최종 잔액: " + point.getBalance());
    }

    @Test
    @DisplayName("withOptimisticLock - usePoint")
    void withOptimisticLock_use() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

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

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();

        assertThat(point.getBalance()).isEqualTo(failCount.get() * 100L);
        System.out.println("낙관적 락 최종 잔액: " + point.getBalance());
    }

    private void setupForRefund() {
        for (Long orderId : orderIds) {
            pointService.usePoint(memberId, orderId);
        }
    }

    @Test
    @DisplayName("NO_LOCK - refundPoint")
    void withoutLock_refund() throws InterruptedException {
        setupForRefund();

        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointService.refundPoint(memberId, orderId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();
        assertThat(point.getBalance()).isNotEqualTo(10000L);
        System.out.println("락 없는 최종 환불 잔액: " + point.getBalance());
    }

    @Test
    @DisplayName("withOptimisticLock - refundPoint")
    void withOptimisticLock_refund() throws InterruptedException {
        setupForRefund();

        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointOptimisticLockService.refundPoint(memberId, orderId);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();
        assertThat(point.getBalance()).isEqualTo(10000L - failCount.get() * 100L);
        System.out.println("낙관적 락 최종 환불 잔액: " + point.getBalance());
    }

    @Test
    @DisplayName("NO_LOCK - earnPoint")
    void withoutLock_earn() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointService.earnPoint(memberId, orderId, 10000L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();
        assertThat(point.getBalance()).isNotEqualTo(20000L);
        System.out.println("락 없는 최종 적립 잔액: " + point.getBalance());
    }

    @Test
    @DisplayName("withOptimisticLock - earnPoint")
    void withOptimisticLock_earn() throws InterruptedException {
        int threadCount = 100;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final Long orderId = orderIds.get(i);
            executorService.submit(() -> {
                try {
                    pointOptimisticLockService.earnPoint(memberId, orderId, 10000L);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();
        assertThat(point.getBalance()).isEqualTo(20000L - failCount.get() * 100L);
        System.out.println("낙관적 락 최종 적립 잔액: " + point.getBalance());
    }

    @Test
    @DisplayName("NO_LOCK - earnEventPoint")
    void withoutLock_earnEvent() throws InterruptedException {
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.earnEventPoint(memberId, 1000L, "추천인 보상");
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();
        assertThat(point.getBalance()).isNotEqualTo(20000L); // 기존 10000 + 10*1000
        System.out.println("락 없는 최종 이벤트 적립 잔액: " + point.getBalance());
    }

    @Test
    @DisplayName("withOptimisticLock - earnEventPoint")
    void withOptimisticLock_earnEvent() throws InterruptedException {
        int threadCount = 10;
        AtomicInteger failCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointOptimisticLockService.earnEventPoint(memberId, 1000L, "추천인 보상");
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Point point = pointRepository.findByMemberId(memberId).orElseThrow();
        assertThat(point.getBalance()).isEqualTo(20000L - failCount.get() * 1000L);
        System.out.println("낙관적 락 최종 이벤트 적립 잔액: " + point.getBalance());
    }
}