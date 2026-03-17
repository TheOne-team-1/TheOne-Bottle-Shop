package one.theone.server.domain.payment.dto.request;

public record PaymentCreateRequest(
        Long orderId
        , Long totalAmount
        , Long pointAmount
) {
}
