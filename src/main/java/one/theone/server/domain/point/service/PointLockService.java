package one.theone.server.domain.point.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.redis.RedisLockService;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CommonExceptionEnum;
import one.theone.server.common.exception.domain.PointExceptionEnum;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PointLockService {

    private final RedisLockService redisLockService;
    private final PointService pointService;

    private static final String LOCK_PREFIX = "point:lock:";
    private static final long LOCK_WAIT = 10;
    private static final long LOCK_LEASE = 5L;

    private void executeWithLock(Long memberId, Runnable task) {
        String lockKey = LOCK_PREFIX + memberId;
        String lockValue = null;
        ScheduledFuture<?> watchDog = null;

        try {
            lockValue = redisLockService.tryLock(lockKey, LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS);

            if (lockValue == null) {
                throw new ServiceErrorException(PointExceptionEnum.ERR_POINT_LOCK_FAILED);
            }

            watchDog = redisLockService.setWatchDog(lockKey, lockValue, LOCK_LEASE, TimeUnit.SECONDS);
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceErrorException(CommonExceptionEnum.ERR_GET_REDIS_LOCK_FAIL);
        } finally {
            if (watchDog != null) {
                watchDog.cancel(true);
            }
            if (lockValue != null) {
                redisLockService.unLock(lockKey, lockValue);
            }
        }
    }

    public void usePoint(Long memberId, Long orderId) {
        executeWithLock(memberId, () -> pointService.usePoint(memberId, orderId));
    }

    public void refundPoint(Long memberId, Long orderId) {
        executeWithLock(memberId, () -> pointService.refundPoint(memberId, orderId));
    }

    public void earnPoint(Long memberId, Long orderId, Long finalAmount) {
        executeWithLock(memberId, () -> pointService.earnPoint(memberId, orderId, finalAmount));
    }

    public void earnEventPoint(Long memberId, Long amount, String description) {
        executeWithLock(memberId, () -> pointService.earnEventPoint(memberId, amount, description));
    }
}