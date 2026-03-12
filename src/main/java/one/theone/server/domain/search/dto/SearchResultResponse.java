package one.theone.server.domain.search.dto;

import one.theone.server.common.dto.PageResponse;

public record SearchResultResponse(PageResponse<ProductSearchResponse> page, String suggestion) {
}
