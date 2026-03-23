package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.product.dto.RecentlyViewedProductsGetResponse;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductRecentlyViewedService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    private static final String KEY_PREFIX = "product:recently:viewed:";
    private static final int MAX_SIZE = 3;

    public void record(Long memberId, Long productId) {
        String key = KEY_PREFIX + memberId;
        ListOperations<String, Object> ops = redisTemplate.opsForList();

        ops.remove(key, 0, productId);
        ops.leftPush(key, productId);
        ops.trim(key, 0, MAX_SIZE - 1);
        redisTemplate.expire(key, 1, TimeUnit.DAYS);
    }

    public List<RecentlyViewedProductsGetResponse> getRecentlyViewed(Long memberId) {
        String key = KEY_PREFIX + memberId;
        List<Object> productIds = redisTemplate.opsForList().range(key, 0, MAX_SIZE - 1);
        if (productIds == null) {
            return List.of();
        }
        return productIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .map(productRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(RecentlyViewedProductsGetResponse::from)
                .toList();
    }
}
