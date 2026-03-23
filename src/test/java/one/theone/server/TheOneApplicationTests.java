package one.theone.server;

import one.theone.server.domain.point.event.PointEarnPublisher;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class TheOneApplicationTests {

    @MockitoBean
    private KomoranCorrector komoranCorrector;

    @MockitoBean
    private PointEarnPublisher pointEarnPublisher;

    @MockitoBean
    private RedissonClient redissonClient;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    void contextLoads() {
    }

}
