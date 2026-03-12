package one.theone.server.domain.cart.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CartExceptionEnum;
import one.theone.server.domain.cart.dto.request.CartAddRequest;
import one.theone.server.domain.cart.dto.response.CartAddResponse;
import one.theone.server.domain.cart.dto.response.CartItemResponse;
import one.theone.server.domain.cart.dto.response.CartResponse;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {
    private final RedisTemplate <String, Object> redisTemplate;
    private final ProductRepository productRepository;

    @Transactional
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
            Object quantityValue = cartEntries.get(product.getId().toString()).toString();

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

        Long totalAmount = items.stream().mapToLong(CartItemResponse::lineAmount).sum();

        return new CartResponse(items, totalAmount);
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
        return "cart:member:" + memberId;
    }
}
