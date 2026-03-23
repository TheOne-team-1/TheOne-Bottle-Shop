package one.theone.server.domain.refund.dto.request;

public record RefundCreateRequest(
        Long orderId,
        String reasonDescription
) {
}
