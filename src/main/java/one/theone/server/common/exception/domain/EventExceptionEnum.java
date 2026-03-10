package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum EventExceptionEnum implements ErrorCode {
    ERR_EVENT_END_BEFORE_START(HttpStatus.BAD_REQUEST, "이벤트 종료일은 시작일보다 빠를 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;

    EventExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
