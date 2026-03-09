package one.theone.server.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.annotation.RedisLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

@Slf4j
@Aspect
@Component
@Order(1) // 항상 첫 실행 필요
@RequiredArgsConstructor
public class RedisLettuceLockAspect {
    private final one.theone.server.common.config.redis.RedisLettuceLock redisLettuceLock;
    private final AopInTransaction aopForTransaction;

    @Around("@annotation(redisLock)")
    public Object lock(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String key = "lock:" + redisLock.key();

        String lockValue = redisLettuceLock.tryLock(
                key
                , redisLock.waitTime()
                , redisLock.leaseTime()
                , redisLock.timeUnit()
        );

        if (lockValue == null) {
            throw new IllegalStateException("락 획득 실패");
        }

        // WatchDog 시작
        ScheduledFuture<?> watchDog = redisLettuceLock.setWatchDog(
                key
                , lockValue
                , redisLock.leaseTime()
                , redisLock.timeUnit()
        );

        try {
            return aopForTransaction.proceed(joinPoint);
        } finally {
            watchDog.cancel(true); // WatchDog 종료
            redisLettuceLock.unLock(key, lockValue); // 락 해제
        }
    }
}
