package one.theone.server.domain.product.service;

import com.redis.testcontainers.RedisContainer;
import one.theone.server.domain.product.dto.BestProductsGetResponse;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class ProductViewServiceTest {

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
    private ProductViewService productViewService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @MockitoBean
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 IP는 조회수 중복 카운트 안됨")
    void record_dedup() {
        // given
        Long productId = 1L;
        String clientIp = "127.0.0.1";

        // when
        productViewService.record(productId, clientIp);
        productViewService.record(productId, clientIp);
        productViewService.record(productId, clientIp);

        // then
        assertThat(productViewService.getViewCount(productId)).isEqualTo(1L);
    }

    @Test
    @DisplayName("다른 IP는 각각 조회수 카운트")
    void record_different_ip() {
        // given
        Long productId = 1L;

        // when
        productViewService.record(productId, "1.1.1.1");
        productViewService.record(productId, "2.2.2.2");
        productViewService.record(productId, "3.3.3.3");

        // then
        assertThat(productViewService.getViewCount(productId)).isEqualTo(3L);
    }

    @Test
    @DisplayName("베스트 상품 조회 성공")
    void getBestProducts_success() {
        // given
        Long productId = 1L;
        Product product = Product.register("베스트 상품", 50000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);

        productViewService.record(productId, "1.1.1.1");

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        List<BestProductsGetResponse> result = productViewService.getBestProducts();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("베스트 상품");
    }

}