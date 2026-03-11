package one.theone.server.domain.cart.dto.request;

public record CartAddRequest (
        Long productId,
        Integer quantity
){
}
