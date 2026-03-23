package one.theone.server.common.config.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {
    private static final long INTERVAL_WAIT_TIME = 100;
    private static final long WATCH_DOG_INCREMENT_TIME = 1000;

    private final RedisLockRepository redisLockRepository;

    private final ScheduledExecutorService watchDogExecutor = Executors.newScheduledThreadPool(8);

    // 락 시도
    public String tryLock(String key, long waitTime, long leaseTime, TimeUnit unit)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + unit.toMillis(waitTime);
        while (System.currentTimeMillis() < deadline) {
            String lockValue = redisLockRepository.getLock(key, leaseTime, unit); // ← 위임
            if (lockValue != null) return lockValue;
            Thread.sleep(INTERVAL_WAIT_TIME);
        }
        return null;
    }

    // 락 해제
    public void unLock(String key, String lockValue) {
        redisLockRepository.checkOwnLock(key, lockValue);
    }

    // WatchDog
    public ScheduledFuture<?> setWatchDog(String key, String lockValue, long leaseTime, TimeUnit unit) {
        return watchDogExecutor.scheduleAtFixedRate(() -> redisLockRepository.setWatchDog(key, lockValue, leaseTime, unit)
                , WATCH_DOG_INCREMENT_TIME, WATCH_DOG_INCREMENT_TIME, TimeUnit.MILLISECONDS
        );
    }
}
