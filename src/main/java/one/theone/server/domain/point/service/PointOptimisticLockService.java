package one.theone.server.domain.point.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.PointExceptionEnum;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointOptimisticLockService {

    private final PointService pointService;

    private static final int MAX_RETRY = 5;

    private void executeWithOptimisticLock(Long memberId, Runnable task) {
        int retry = 0;
        while (retry < MAX_RETRY) {
            try {
                task.run();
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                retry++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ServiceErrorException(PointExceptionEnum.ERR_POINT_LOCK_FAILED);
                }
            }
        }
        throw new ServiceErrorException(PointExceptionEnum.ERR_POINT_LOCK_FAILED);
    }

    public void usePoint(Long memberId, Long orderId) {
        executeWithOptimisticLock(memberId, () -> pointService.usePoint(memberId, orderId));
    }

    public void refundPoint(Long memberId, Long orderId) {
        executeWithOptimisticLock(memberId, () -> pointService.refundPoint(memberId, orderId));
    }

    public void earnPoint(Long memberId, Long orderId, Long finalAmount) {
        executeWithOptimisticLock(memberId, () -> pointService.earnPoint(memberId, orderId, finalAmount));
    }

    public void earnEventPoint(Long memberId, Long amount, String description) {
        executeWithOptimisticLock(memberId, () -> pointService.earnEventPoint(memberId, amount, description));
    }
}
