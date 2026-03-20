package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductOptimisticLockService {

    private final ProductService productService;

    private static final int MAX_RETRY = 5;

    private void executeWithOptimisticLock(Runnable task) {
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
                    throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_LOCK_FAILED);
                }
            }
        }
        throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_LOCK_FAILED);
    }

    public void decreaseStock(Long productId, Long quantity) {
        executeWithOptimisticLock(() -> productService.decreaseStock(productId, quantity));
    }

    public void increaseStock(Long productId, Long quantity) {
        executeWithOptimisticLock(() -> productService.increaseStock(productId, quantity));
    }
}
