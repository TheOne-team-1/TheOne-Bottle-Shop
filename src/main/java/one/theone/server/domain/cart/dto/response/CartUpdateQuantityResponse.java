package one.theone.server.domain.cart.dto.response;

public record CartUpdateQuantityResponse(
        Long productId,
        Integer quantity
) {
}
