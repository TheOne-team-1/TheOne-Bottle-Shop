package one.theone.server.domain.point.event;

import com.redis.testcontainers.RedisContainer;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.point.dto.PointAdjustRequest;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.entity.PointLog;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
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

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class PointPubSubTest {

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
    private PointEarnPublisher pointEarnPublisher;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Long memberId;

    @BeforeEach
    void setUp() {
        String email = UUID.randomUUID() + "@test.com";
        String recommendCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Member member = memberRepository.save(
                Member.create(email, "password", "테스트", "20000101", recommendCode));
        memberId = member.getId();

        PointLog earnLog = PointLog.ofAdmin(memberId, new PointAdjustRequest(0L, "초기화"), 0L);
        pointLogRepository.save(earnLog);
        pointRepository.save(Point.register(memberId));
    }

    @AfterEach
    void tearDown() {
        pointLogRepository.deleteAll();
        pointRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("추천인 포인트 적립")
    void publish_recommend_event_earn_point() {
        // given
        RedisPointEarnEvent event = new RedisPointEarnEvent(memberId, 500L, "추천인 보상");

        // when
        pointEarnPublisher.publish(event);

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Point point = pointRepository.findByMemberId(memberId).orElseThrow();
            assertThat(point.getBalance()).isEqualTo(500L);
        });
    }

    @Test
    @DisplayName("리뷰 포인트 적립")
    void publish_review_event_earn_point() {
        // given
        RedisPointEarnEvent event = new RedisPointEarnEvent(memberId, 500L, "리뷰 작성 포인트 지급");

        // when
        pointEarnPublisher.publish(event);

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Point point = pointRepository.findByMemberId(memberId).orElseThrow();
            assertThat(point.getBalance()).isEqualTo(500L);
        });
    }
}
