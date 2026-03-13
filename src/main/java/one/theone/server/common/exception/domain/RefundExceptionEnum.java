package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum RefundExceptionEnum implements ErrorCode {
    ERR_INVALID_PENDING(HttpStatus.CONFLICT, "환불 대기 상태에서만 가능합니다");

    private final HttpStatus httpStatus;
    private final String message;

    RefundExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
