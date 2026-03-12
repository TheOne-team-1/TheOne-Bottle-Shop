package one.theone.server.domain.cart.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CartExceptionEnum;
import one.theone.server.domain.cart.dto.request.CartAddRequest;
import one.theone.server.domain.cart.dto.response.CartAddResponse;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
    private final RedisTemplate <String, Object> redisTemplate;
    private final ProductRepository productRepository;

    public CartAddResponse addItem(Long memberId, CartAddRequest request) {
        validateRequest(request);

        if (!productRepository.existsById(request.productId())) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_PRODUCT_NOT_FOUND);
        }

        String cartKey = generateCartKey(memberId);
        redisTemplate.opsForHash().increment(
                cartKey,
                request.productId().toString(),
                request.quantity()
        );

        Object savedQuantity = redisTemplate.opsForHash()
                .get(cartKey, request.productId().toString());

        return new CartAddResponse(
                request.productId(),
                Integer.parseInt(savedQuantity.toString())
        );
    }

    private void validateRequest(CartAddRequest request) {
        if (request.productId() == null || request.productId() < 1) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_CART_INVALID_PRODUCT_ID);
        }

        if (request.quantity() == null || request.quantity() < 1) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_CART_INVALID_QUANTITY);
        }
    }

    private String generateCartKey(Long memberId) {
        return "cart:" + memberId;
    }
}
