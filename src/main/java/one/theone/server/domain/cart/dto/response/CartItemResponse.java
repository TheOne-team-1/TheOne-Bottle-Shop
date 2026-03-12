package one.theone.server.domain.cart.dto.response;

import java.math.BigDecimal;

public record CartItemResponse (
        Long productId,
        String productName,
        Long price,
        Integer quantity,
        Long lineAmount
){
}
