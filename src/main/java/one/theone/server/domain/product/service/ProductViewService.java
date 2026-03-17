package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.redis.RedisLockService;
import one.theone.server.domain.product.dto.BestProductsGetResponse;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductViewService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLockService redisLockService;

    private static final String LOCK_PREFIX = "product:view:lock:";
    private static final long LOCK_WAIT = 1L;
    private static final long LOCK_LEASE = 3L;

    private static final String DEDUP_PREFIX = "product:view:dedup:";
    private static final long DEDUP_TTL = 60 * 60 * 24 * 7; // 7일

    private static final String VIEW_COUNT_PREFIX = "product:viewCount:week:";
    private static final int BEST_PRODUCTS_LIMIT = 4;
    private static final int BEST_PRODUCTS_BUFFER = 4*2;

    private final ProductRepository productRepository;

    public void record(Long productId, String clientIp) {
        String lockKey = LOCK_PREFIX + productId + ":" + clientIp;
        String lockValue = null;

        try {
            lockValue = redisLockService.tryLock(lockKey, LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS);

            if (lockValue == null) {
                return;
            }
            String dedupKey = DEDUP_PREFIX + productId + ":" + clientIp;
            Boolean isFirstView = redisTemplate.opsForValue().setIfAbsent(dedupKey, "locked", DEDUP_TTL, TimeUnit.SECONDS);

            if (Boolean.TRUE.equals(isFirstView)) {
                String viewCountKey = getWeeklyViewCountKey();
                redisTemplate.opsForZSet().incrementScore(viewCountKey, productId.toString(), 1);
                if (redisTemplate.getExpire(viewCountKey) == -1L) {
                    redisTemplate.expire(viewCountKey, 8, TimeUnit.DAYS);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lockValue != null) {
                redisLockService.unLock(lockKey, lockValue);
            }
        }
    }

    public Long getViewCount(Long productId) {
        String viewCountKey = getWeeklyViewCountKey();
        Double score = redisTemplate.opsForZSet().score(viewCountKey, productId.toString());
        return score != null ? score.longValue() : 0L;
    }

    public List<BestProductsGetResponse> getBestProducts() {
        String viewCountKey = getWeeklyViewCountKey();
        Set<Object> productIds = redisTemplate.opsForZSet().reverseRange(viewCountKey, 0, BEST_PRODUCTS_BUFFER);
        if (productIds == null) return List.of();
        return productIds.stream()
                .map(id -> Long.parseLong((String) id))
                .map(productRepository::findById)
                .filter(Optional::isPresent)
                .limit(BEST_PRODUCTS_LIMIT)
                .map(Optional::get)
                .map(BestProductsGetResponse::from)
                .toList();
    }

    private String getWeeklyViewCountKey() {
        LocalDate now = LocalDate.now();
        int weekNumber = now.get(WeekFields.of(Locale.KOREA).weekOfYear());
        return VIEW_COUNT_PREFIX + now.getYear() + ":" + weekNumber;
    }
}