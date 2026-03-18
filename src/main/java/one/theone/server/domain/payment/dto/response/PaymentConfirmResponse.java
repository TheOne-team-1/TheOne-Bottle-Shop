package one.theone.server.domain.payment.dto.response;

public record PaymentConfirmResponse(
    Long orderId
    , Long paymentId
    , String paymentUniqueId
) {
}
