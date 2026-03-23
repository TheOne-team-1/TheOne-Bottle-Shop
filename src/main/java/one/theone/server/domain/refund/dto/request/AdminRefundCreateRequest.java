package one.theone.server.domain.refund.dto.request;

import one.theone.server.domain.refund.entity.Refund;

public record AdminRefundCreateRequest(
        Long orderId
        , Refund.RefundReason reason
        , String reasonDescription
) {
}
