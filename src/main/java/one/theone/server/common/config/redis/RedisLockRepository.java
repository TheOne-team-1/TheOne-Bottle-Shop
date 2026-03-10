package one.theone.server.common.config.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// Redis 연산 담당 부
@Component
@RequiredArgsConstructor
public class RedisLockRepository {
    private final RedisTemplate<String, Object> redisTemplate;

    // 락 획득하기
    public String getLock(String key, long leaseTime, TimeUnit unit) {
        String lockValue = UUID.randomUUID().toString();// 개별 LockValue 를 위한 UUID 셋업
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, lockValue, leaseTime, unit);
        return Boolean.TRUE.equals(result) ? lockValue : null;
    }

    // 원자성 체크 (자기 락 확인)
    public void checkOwnLock(String key, String lockValue) {
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('del', KEYS[1]) " +
                        "else return 0 end";
        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key), lockValue
        );
    }

    // WatchDog - 원자성 체크 후 아직 내 락이면 TTL 자동 연장
    public void setWatchDog(String key, String lockValue, long leaseTime, TimeUnit unit) {
        String luaScript =
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                        "    return redis.call('expire', KEYS[1], ARGV[2]) " +
                        "else return 0 end";
        redisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                Collections.singletonList(key),
                lockValue, String.valueOf(unit.toSeconds(leaseTime))
        );
    }
}
