package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum EventExceptionEnum implements ErrorCode {
    ERR_EVENT_END_BEFORE_START(HttpStatus.BAD_REQUEST, "이벤트 종료일은 시작일보다 빠를 수 없습니다"),
    ERR_EVENT_TYPE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 이벤트 타입입니다"),
    ERR_EVENT_PRODUCT_REQUIRED(HttpStatus.BAD_REQUEST, "상품 조건 이벤트는 상품 ID가 필요합니다"),
    ERR_EVENT_MIN_PRICE_REQUIRED(HttpStatus.BAD_REQUEST, "금액 조건 이벤트는 최소 금액이 필요합니다"),
    ERR_EVENT_DETAIL_INVALID(HttpStatus.BAD_REQUEST, "이벤트 상세 조건은 하나만 설정할 수 있습니다"),
    ERR_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "이벤트를 찾을 수 없습니다"),
    ERR_EVENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 이벤트 상태입니다");


    private final HttpStatus httpStatus;
    private final String message;

    EventExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
