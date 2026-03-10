package one.theone.server.domain.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductsGetRequest(
        ProductSortType sortType,
        List<Long> categoryIds,
        BigDecimal abvMin,
        BigDecimal abvMax,
        Long priceMin,
        Long priceMax,
        List<Integer> volumeMl
) {
    public ProductsGetRequest {
        if (sortType == null) {
            sortType = ProductSortType.LATEST;
        }
    }

    public enum ProductSortType {
        LATEST, PRICE_ASC, PRICE_DESC, RATING_DESC;
    }
}


