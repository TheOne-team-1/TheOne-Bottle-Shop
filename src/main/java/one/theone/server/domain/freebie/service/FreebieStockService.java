package one.theone.server.domain.freebie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.config.redis.RedisLockService;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CommonExceptionEnum;
import one.theone.server.common.exception.domain.FreebieExceptionEnum;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static one.theone.server.common.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreebieStockService {
    private final FreebieService freebieService;
    private final RedisLockService redisLockService;

    public void executeWithLock(Long id, Runnable runnable) {
        String key = FREEBIE_LOCK_KEY + id;

        String lockValue = null;
        ScheduledFuture<?> watchDog = null;

        try {
            lockValue = redisLockService.tryLock(key, FREEBIE_LOCK_WAIT_TIME, FREEBIE_LOCK_LEASE_TIME, TimeUnit.SECONDS);

            if(lockValue == null) {
                log.error("사은품 재고 락 획득 실패");
                throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_LOCK_FAILED);
            }

            watchDog = redisLockService.setWatchDog(key, lockValue, FREEBIE_LOCK_WATCH_DOG_LEASE_TIME, TimeUnit.SECONDS);

            runnable.run();
        } catch (InterruptedException e) {
            log.error("사은품 재고 락 작동 오류 : {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new ServiceErrorException(CommonExceptionEnum.ERR_GET_REDIS_LOCK_FAIL);
        } finally {
            if(watchDog != null) {
                watchDog.cancel(true);
            }

            if(lockValue != null) {
                redisLockService.unLock(key, lockValue);
            }
        }
    }

    public void decreaseStockWithLock(Long id, Long quantity) {
        executeWithLock(id, () -> freebieService.decreaseStock(id, quantity));
    }

    public void increaseStockWithLock(Long id, Long quantity) {
        executeWithLock(id, () -> freebieService.increaseStock(id, quantity));
    }
}
