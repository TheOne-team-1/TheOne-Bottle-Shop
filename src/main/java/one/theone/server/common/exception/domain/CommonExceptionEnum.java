package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonExceptionEnum implements ErrorCode {
    ERR_GET_REDIS_LOCK_FAIL(HttpStatus.CONFLICT, "서버 오류 처리 실패, 락 획득에 실패하였습니다");

    private final HttpStatus httpStatus;
    private final String message;

    CommonExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
