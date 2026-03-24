package one.theone.server.domain.product.service;

import com.redis.testcontainers.RedisContainer;
import one.theone.server.domain.product.dto.RecentlyViewedProductsGetResponse;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class ProductRecentlyViewedServiceTest {

    @Container
    static final RedisContainer redisContainer = new RedisContainer(
            DockerImageName.parse("redis:8.6.1")).withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redisContainer.getHost());
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @MockitoBean
    private KomoranCorrector komoranCorrector;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private ProductRecentlyViewedService productRecentlyViewedService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("최대 3개만 저장됨")
    void record_maxSize() {
        // given
        Long memberId = 1L;

        // when
        productRecentlyViewedService.record(memberId, 1L);
        productRecentlyViewedService.record(memberId, 2L);
        productRecentlyViewedService.record(memberId, 3L);
        productRecentlyViewedService.record(memberId, 4L);  // 4번째 → 1번 밀려남

        List<Object> stored = redisTemplate.opsForList().range("product:recently:viewed:1", 0, -1);

        // then
        assertThat(stored).hasSize(3);
        assertThat(stored.get(0).toString()).isEqualTo("4");  // 가장 최근
        assertThat(stored.get(2).toString()).isEqualTo("2");  // 1번 밀려남
    }

    @Test
    @DisplayName("중복 상품은 맨 앞으로 이동")
    void record_dedup() {
        // given
        Long memberId = 1L;

        // when
        productRecentlyViewedService.record(memberId, 1L);
        productRecentlyViewedService.record(memberId, 2L);
        productRecentlyViewedService.record(memberId, 1L);  // 중복

        List<Object> stored = redisTemplate.opsForList().range("product:recently:viewed:1", 0, -1);

        // then
        assertThat(stored).hasSize(2);           // 중복 제거로 2개
        assertThat(stored.get(0).toString()).isEqualTo("1");  // 맨 앞으로
    }

    @Test
    @DisplayName("최근 본 상품 조회 성공")
    void getRecentlyViewed_success() {
        // given
        Long memberId = 1L;
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);

        productRecentlyViewedService.record(memberId, productId);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        List<RecentlyViewedProductsGetResponse> result = productRecentlyViewedService.getRecentlyViewed(memberId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("테스트 상품");
    }

    @Test
    @DisplayName("최근 본 상품 없으면 빈 리스트 반환")
    void getRecentlyViewed_empty() {
        // when
        List<RecentlyViewedProductsGetResponse> result = productRecentlyViewedService.getRecentlyViewed(999L);

        // then
        assertThat(result).isEmpty();
    }
}