package one.theone.server.domain.cart.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CartExceptionEnum;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.cart.dto.request.CartAddRequest;
import one.theone.server.domain.cart.dto.request.CartUpdateQuantityRequest;
import one.theone.server.domain.cart.dto.response.CartAddResponse;
import one.theone.server.domain.cart.dto.response.CartItemResponse;
import one.theone.server.domain.cart.dto.response.CartRemoveItemResponse;
import one.theone.server.domain.cart.dto.response.CartResponse;
import one.theone.server.domain.cart.dto.response.CartUpdateQuantityResponse;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    @Transactional
    public CartAddResponse addItem(Long memberId, CartAddRequest request) {
        validateAddRequest(request);

        if (!productRepository.existsById(request.productId())) {
            throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_NOT_FOUND);
        }

        String cartKey = generateCartKey(memberId);
        String field = generateCartField(request.productId());

        redisTemplate.opsForHash().increment(
                cartKey,
                field,
                request.quantity()
        );

        Object savedQuantity = redisTemplate.opsForHash().get(cartKey, field);

        return new CartAddResponse(
                request.productId(),
                Integer.parseInt(savedQuantity.toString())
        );
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long memberId) {
        String cartKey = generateCartKey(memberId);

        Map<Object, Object> cartEntries = redisTemplate.opsForHash().entries(cartKey);

        if (cartEntries.isEmpty()) {
            return new CartResponse(
                    Collections.emptyList(),
                    0L
            );
        }

        List<Long> productIds = cartEntries.keySet().stream()
                .map(key -> Long.valueOf(key.toString()))
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        List<CartItemResponse> items = new ArrayList<>();

        for (Product product : products) {
            Object quantityValue = cartEntries.get(product.getId().toString());

            if (quantityValue == null) {
                continue;
            }

            Integer quantity = Integer.valueOf(quantityValue.toString());
            Long lineAmount = product.getPrice() * quantity;

            items.add(new CartItemResponse(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    quantity,
                    lineAmount
            ));
        }

        Long totalAmount = items.stream()
                .mapToLong(CartItemResponse::lineAmount)
                .sum();

        return new CartResponse(items, totalAmount);
    }

    @Transactional
    public CartUpdateQuantityResponse updateQuantity(
            Long memberId, Long productId, CartUpdateQuantityRequest request
    ) {
        validateUpdateQuantityRequest(productId, request);

        String cartKey = generateCartKey(memberId);
        String field = generateCartField(productId);

        validateCartItemExists(cartKey, field);

        redisTemplate.opsForHash().put(cartKey, field, request.quantity());

        return new CartUpdateQuantityResponse(
                productId,
                request.quantity()
        );
    }

    @Transactional
    public CartRemoveItemResponse removeItem(Long memberId, Long productId) {
        validateProductId(productId);

        String cartKey = generateCartKey(memberId);
        String field = generateCartField(productId);

        validateCartItemExists(cartKey, field);

        redisTemplate.opsForHash().delete(cartKey, field);

        return new CartRemoveItemResponse(productId);
    }

    private void validateAddRequest(CartAddRequest request) {
        validateProductId(request.productId());
        validateQuantity(request.quantity());
    }

    private void validateUpdateQuantityRequest(Long productId, CartUpdateQuantityRequest request) {
        validateProductId(productId);
        validateQuantity(request.quantity());
    }

    private void validateProductId(Long productId) {
        if (productId == null || productId < 1) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_CART_INVALID_PRODUCT_ID);
        }
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_CART_INVALID_QUANTITY);
        }
    }

    private void validateCartItemExists(String cartKey, String field) {
        Boolean exists = redisTemplate.opsForHash().hasKey(cartKey, field);

        if (!Boolean.TRUE.equals(exists)) {
            throw new ServiceErrorException(CartExceptionEnum.ERR_CART_ITEM_NOT_FOUND);
        }
    }

    private String generateCartKey(Long memberId) {
        return "cart:member:" + memberId;
    }

    private String generateCartField(Long productId) {
        return productId.toString();
    }
}