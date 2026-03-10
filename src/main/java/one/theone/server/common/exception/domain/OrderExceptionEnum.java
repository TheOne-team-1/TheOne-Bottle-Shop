package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderExceptionEnum implements ErrorCode {
    ERR_ORDER_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "최종 주문 금액은 0보다 작을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;

    OrderExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
