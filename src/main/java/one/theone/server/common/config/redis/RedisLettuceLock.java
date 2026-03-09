package one.theone.server.common.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLettuceLock {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final long INTERVAL_WAIT_TIME = 100;
    private static final long WATCH_DOG_INCREMENT_TIME = 1000;

    // Lock 획득하기
    public String getLock(String key, long leaseTime, TimeUnit unit) {
        // 내가 건 락 인지의 증명을 위한 UUID
        String lockValue = UUID.randomUUID().toString();

        // Redis 키가 있으면 true, 없으면 생성하고 False
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, lockValue, leaseTime, unit);
        return Boolean.TRUE.equals(result) ? lockValue : null;
    }

    // Lock 풀기
    public void unLock(String key, String lockValue) {
        // 원자적인 처리를 위한 Lua Script
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('del', KEYS[1]) " +
                        "else " +
                        "    return 0 " +
                        "end";

        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key),
                lockValue
        );
    }

    // SpinLock 시도
    public String tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeUnit.toMillis(waitTime); // 현재 시간 + waitTime = 데드라인

        // 데드라인 전 까지 계속 재시도
        while (System.currentTimeMillis() < deadline) {
            String lockValue = getLock(key, leaseTime, timeUnit); // 재시도
            if (lockValue != null) return lockValue;
            Thread.sleep(INTERVAL_WAIT_TIME); // 과부하 방지 FIXME: 바쁜 대기에 빠질 수 있음
        }

        // 데드라인 초과시 null
        return null;
    }

    // WatchDog (TTL 자동 연장)
    public ScheduledFuture<?> setWatchDog(String key, String lockValue, long leaseTime, TimeUnit timeUnit) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        //원자적인 값이 동일하면 만료시간을 늘림
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('expire', KEYS[1], ARGV[2]) " +
                        "else " +
                        "    return 0 " +
                        "end";

        return scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                redisTemplate.execute(
                        new DefaultRedisScript<>(luaScript, Long.class),
                        Collections.singletonList(key),
                        lockValue,
                        String.valueOf(timeUnit.toSeconds(leaseTime))
                );
                log.debug("WatchDog, TTL 연장 key = {}", key);
            } catch (Exception e) {
                log.error("WatchDog, 오류 발생 key = {}", key, e);
            }
        }, WATCH_DOG_INCREMENT_TIME, WATCH_DOG_INCREMENT_TIME, TimeUnit.MILLISECONDS);
    }
}
