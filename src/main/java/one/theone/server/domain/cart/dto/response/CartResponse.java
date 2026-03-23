package one.theone.server.domain.cart.dto.response;

import java.util.List;

public record CartResponse (
        List<CartItemResponse> items,
        Long totalAmount
){
}
