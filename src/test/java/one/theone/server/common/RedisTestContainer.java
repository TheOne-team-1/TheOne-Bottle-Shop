package one.theone.server.common;

import com.redis.testcontainers.RedisContainer;
import one.theone.server.domain.point.event.PointEarnPublisher;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.utility.DockerImageName;


// 모든 Redis 통합 테스트
// Redis 컨테이너를 JVM 내에서 한 번만 기동하고 전체 테스트가 공유
@SpringBootTest
@ActiveProfiles("test")
public abstract class RedisTestContainer {

    @MockitoBean
    KomoranCorrector komoranCorrector;

    @MockitoBean
    PointEarnPublisher pointEarnPublisher;

    // static 블록으로 JVM 전체에서 단 한 번만 컨테이너 기동, Singleton
    static final RedisContainer redisContainer;

    static {
        redisContainer = new RedisContainer(DockerImageName.parse("redis:8.6.1"))
                .withExposedPorts(6379);
        redisContainer.start();
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }
}
