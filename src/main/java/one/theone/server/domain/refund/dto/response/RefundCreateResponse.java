package one.theone.server.domain.refund.dto.response;

import one.theone.server.domain.refund.entity.Refund;

import java.time.LocalDateTime;

public record RefundCreateResponse(
        Long refundId
        , Long orderId
        , Long paymentId
        , LocalDateTime refundAt
) {
}
