package one.theone.server.domain.cart.dto.response;

public record CartItemResponse (
        Long productId,
        String productName,
        Long price,
        Long remainingQuantity,
        Integer quantity,
        Long lineAmount
){
}
