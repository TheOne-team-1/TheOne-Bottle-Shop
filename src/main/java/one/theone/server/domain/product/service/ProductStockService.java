package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.redis.RedisLockService;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CommonExceptionEnum;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductStockService {

    private final RedisLockService redisLockService;
    private final ProductService productService;

    private static final String LOCK_PREFIX = "product:stock:lock:";
    private static final long LOCK_WAIT = 3L;
    private static final long LOCK_LEASE = 5L;
    private static final int MAX_RETRY = 3;

    private void executeWithLock(Long productId, Runnable task) {
        String lockKey = LOCK_PREFIX + productId;
        int retry = 0;

        while (retry < MAX_RETRY) {
            String lockValue = null;
            try {
                lockValue = redisLockService.tryLock(lockKey, LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS);

                if (lockValue == null) {
                    retry++;
                    continue;
                }

                task.run();
                return;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ServiceErrorException(CommonExceptionEnum.ERR_GET_REDIS_LOCK_FAIL);
            } finally {
                if (lockValue != null) {
                    redisLockService.unLock(lockKey, lockValue);
                }
            }
        }
        throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_LOCK_FAILED);
    }

    public void decreaseStock(Long productId, Long quantity) {
        executeWithLock(productId, () -> productService.decreaseStock(productId, quantity));
    }

    public void increaseStock(Long productId, Long quantity) {
        executeWithLock(productId, () -> productService.increaseStock(productId, quantity));
    }
}
