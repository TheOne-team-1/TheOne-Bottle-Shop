package one.theone.server.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static one.theone.server.common.config.cache.CacheConfig.SEARCH_RANKING;

@Service
@RequiredArgsConstructor
public class SearchRankingService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DEDUP_PREFIX = "search:dedup:";
    private static final long DEDUP_TTL = 60*30;

    private static final String RANKING_PREFIX = "search:ranking:week:";
    private static final int RANKING_LIMIT = 5;

    @Async("asyncExecutor")
    @CacheEvict(value = SEARCH_RANKING, allEntries = true, cacheManager = "redisCacheManager")
    public void record(String keyword, Long userId, String clientIp, String userAgent) {
        String identifier = (userId != null) ? "user:" + userId : "guest:" + clientIp + ":" + userAgent;
        String dedupKey = DEDUP_PREFIX + keyword + ":" + identifier;
        Boolean isFirstSearch = redisTemplate.opsForValue().setIfAbsent(dedupKey, "locked", DEDUP_TTL, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(isFirstSearch)) {
            String rankingKey = getWeeklyRankingKey();
            redisTemplate.opsForZSet().incrementScore(rankingKey, keyword, 1);
            if (redisTemplate.getExpire(rankingKey) == -1L) {
                redisTemplate.expire(rankingKey, 8, TimeUnit.DAYS);
            }
        }
    }

    @Cacheable(value = SEARCH_RANKING, key = "'ranking:top5'", cacheManager = "redisCacheManager")
    public List<String> getKeywordRanking() {
        String rankingKey = getWeeklyRankingKey();
        Set<Object> keywords = redisTemplate.opsForZSet().reverseRange(rankingKey, 0, RANKING_LIMIT-1);
        if (keywords == null) return List.of();

        return keywords.stream()
                .map(k -> k instanceof String s ? s : k.toString())
                .toList();
    }

    private String getWeeklyRankingKey() {
        LocalDate now = LocalDate.now();
        int weekNumber = now.get(WeekFields.of(Locale.KOREA).weekOfYear());
        return RANKING_PREFIX + now.getYear() + ":" + weekNumber;
    }
}
