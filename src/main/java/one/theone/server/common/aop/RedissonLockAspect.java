package one.theone.server.common.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.annotation.RedissonLock;
import one.theone.server.common.exception.ServiceErrorException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import static one.theone.server.common.exception.domain.CommonExceptionEnum.ERR_GET_REDIS_LOCK_FAIL;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedissonLockAspect {
    private final RedissonClient redissonClient;
    private final AopInTransaction aopInTransaction;

    @Around("@annotation(redissonLock)")
    public Object lock(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        String key = "rLock:" + redissonLock.key();
        RLock rLock = redissonClient.getLock(key);

        boolean isLock = rLock.tryLock( // 실제 Lock 획득
                redissonLock.waitTime()
                , redissonLock.leaseTime()
                , redissonLock.timeUnit()
        );

        if (!isLock) { // 자동 WatchDog 내부 수행
            throw new ServiceErrorException(ERR_GET_REDIS_LOCK_FAIL);
        }

        try {
            return aopInTransaction.proceed(joinPoint);
        } finally {
           if(rLock.isHeldByCurrentThread()) { // 아직 RLock이 쓰레드 점유 시 (자신이 건 락이 맞는지 체크)
               rLock.unlock();
           }
        }
    }
}
