package one.theone.server.domain.product.dto;

public record BestProductsGetResponse(
        Long id,
        String name,
        Long price
) {
}
