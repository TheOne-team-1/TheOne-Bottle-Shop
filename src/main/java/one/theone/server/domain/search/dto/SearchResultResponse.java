package one.theone.server.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.BestProductsGetResponse;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchResultResponse(
        PageResponse<ProductSearchResponse> page,
        String suggestion,
        BestProductsGetResponse recommendedProduct) {
}
