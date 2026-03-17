package one.theone.server.domain.order.dto.response;

import java.util.List;

public record OrderPageResponse(
        List<OrderListGetResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
