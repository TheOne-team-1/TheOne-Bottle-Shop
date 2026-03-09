package one.theone.server.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.annotation.RedisLock;
import one.theone.server.common.config.redis.RedisLettuceLock;
import one.theone.server.common.exception.ServiceErrorException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

import static one.theone.server.common.exception.domain.CommonExceptionEnum.ERR_GET_REDIS_LOCK_FAIL;

@Slf4j
@Aspect
@Component
@Order(1) // 항상 첫 실행 필요
@RequiredArgsConstructor
public class RedisLettuceLockAspect {
    private final RedisLettuceLock redisLettuceLock;
    private final AopInTransaction aopInTransaction;

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
            throw new ServiceErrorException(ERR_GET_REDIS_LOCK_FAIL);
        }

        // WatchDog 시작
        ScheduledFuture<?> watchDog = redisLettuceLock.setWatchDog(
                key
                , lockValue
                , redisLock.leaseTime()
                , redisLock.timeUnit()
        );

        try {
            return aopInTransaction.proceed(joinPoint);
        } finally {
            watchDog.cancel(true); // WatchDog 종료
            redisLettuceLock.unLock(key, lockValue); // 락 해제
        }
    }
}
