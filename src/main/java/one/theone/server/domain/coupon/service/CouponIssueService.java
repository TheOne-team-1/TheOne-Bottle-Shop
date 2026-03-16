package one.theone.server.domain.coupon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.common.config.redis.RedisLockService;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CommonExceptionEnum;
import one.theone.server.domain.coupon.dto.request.CouponIssueEventRequest;
import one.theone.server.domain.coupon.dto.response.CouponIssueResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static one.theone.server.common.exception.domain.CouponExceptionEnum.ERR_COUPON_LOCK_FAILED;
import static one.theone.server.common.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueService {
    private final CouponService couponService;
    private final RedisLockService redisLockService;

    public CouponIssueResponse issueCouponWithLock(Long couponId, Long memberId, Long eventId) {
        AtomicReference<CouponIssueResponse> result = new AtomicReference<>();
        CouponIssueEventRequest request = new CouponIssueEventRequest(eventId);
        executeWithLock(couponId, () -> result.set(couponService.issueCouponByEvent(couponId, memberId, request)));
        return result.get();
    }

    private void executeWithLock(Long couponId, Runnable runnable) {
        String key = COUPON_LOCK_KEY + couponId;

        String lockValue = null;
        ScheduledFuture<?> watchDog = null;

        try {
            lockValue = redisLockService.tryLock(key, COUPON_LOCK_WAIT_TIME, COUPON_LOCK_LEASE_TIME, TimeUnit.SECONDS);

            if (lockValue == null) {
                log.error("쿠폰 발급 락 획득 실패");
                throw new ServiceErrorException(ERR_COUPON_LOCK_FAILED);
            }

            watchDog = redisLockService.setWatchDog(key, lockValue, COUPON_LOCK_WATCH_DOG_LEASE_TIME, TimeUnit.SECONDS);

            runnable.run();
        } catch (InterruptedException e) {
            log.error("쿠폰 발급 락 작동 오류 : {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new ServiceErrorException(CommonExceptionEnum.ERR_GET_REDIS_LOCK_FAIL);
        } finally {
            if (watchDog != null) {
                watchDog.cancel(true);
            }
            if (lockValue != null) {
                redisLockService.unLock(key, lockValue);
            }
        }
    }
}
