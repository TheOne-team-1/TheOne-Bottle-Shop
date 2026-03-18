package one.theone.server.domain.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record AdminProductsGetRequest(
        ProductSortType sortType,
        List<Long> categoryIds,
        BigDecimal abvMin,
        BigDecimal abvMax,
        Long priceMin,
        Long priceMax,
        List<Integer> volumeMl,
        AdminProductsGetResponse.AdminProductsStatus status
) {
    public AdminProductsGetRequest {
        if (sortType == null) {
            sortType = ProductSortType.LATEST;
        }
    }

    public enum ProductSortType {
        LATEST, PRICE_ASC, PRICE_DESC, RATING_DESC
    }
}
