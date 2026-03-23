package one.theone.server.domain.point.event;

import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.point.dto.PointAdjustRequest;
import one.theone.server.domain.point.entity.Point;
import one.theone.server.domain.point.entity.PointLog;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import one.theone.server.common.RedisTestContainer;

public class PointPubSubTestContainer extends RedisTestContainer {




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
