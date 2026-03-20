package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductPessimisticLockService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    @Transactional
    public void decreaseStock(Long productId, Long quantity) {
        productRepository.findByIdWithLock(productId);
        productService.decreaseStock(productId, quantity);
    }

    @Transactional
    public void increaseStock(Long productId, Long quantity) {
        productRepository.findByIdWithLock(productId);
        productService.increaseStock(productId, quantity);
    }
}
