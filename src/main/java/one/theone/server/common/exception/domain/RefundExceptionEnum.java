package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum RefundExceptionEnum implements ErrorCode {
    ERR_INVALID_PENDING(HttpStatus.CONFLICT, "환불 대기 상태에서만 가능합니다"),
    ERR_ORDER_NOT_REFUNDABLE(HttpStatus.CONFLICT, "환불 가능한 주문 상태가 아닙니다"),
    ERR_USE_EVENT_COUPON_NOT_REFUND(HttpStatus.CONFLICT, "이벤트로 지급받은 쿠폰이 사용되어 환불이 불가능 합니다"),
    ERR_REFUND_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 완료된 환불 건입니다");

    private final HttpStatus httpStatus;
    private final String message;

    RefundExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
