package one.theone.server.domain.search.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.redis.RedisLockService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SearchRankingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLockService redisLockService;

    private static final String LOCK_PREFIX = "search:record:";
    private static final long LOCK_WAIT = 1L;
    private static final long LOCK_LEASE = 3L;

    private static final String DEDUP_PREFIX = "search:dedup:";
    private static final long DEDUP_TTL = 60*10;

    private static final String RANKING_KEY = "search:ranking";
    private static final int RANKING_LIMIT = 5;

    public void record(String keyword) {
        String lockKey = LOCK_PREFIX + keyword.trim().toLowerCase();
        String lockValue = null;

        try {
            lockValue = redisLockService.tryLock(lockKey, LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS);

            if (lockValue == null) {
                return;
            }
            String dedupKey = DEDUP_PREFIX + keyword.trim().toLowerCase();
            Boolean isFirstSearch = redisTemplate.opsForValue().setIfAbsent(dedupKey, "locked", DEDUP_TTL, TimeUnit.SECONDS);

            if (isFirstSearch) {
                redisTemplate.opsForZSet().incrementScore(RANKING_KEY, keyword.trim().toLowerCase(), 1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lockValue != null) {
                redisLockService.unLock(lockKey, lockValue);
            }
        }
    }

    public List<String> getKeywordRanking() {
        Set<Object> keywords = redisTemplate.opsForZSet().reverseRange(RANKING_KEY, 0, RANKING_LIMIT-1);
        if (keywords == null) return Collections.emptyList();

        return keywords.stream()
                .map(k -> (String) k)
                .toList();
    }
}
