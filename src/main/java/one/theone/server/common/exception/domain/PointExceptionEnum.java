package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum PointExceptionEnum implements ErrorCode {
    ERR_INSUFFICIENT_POINT(HttpStatus.BAD_REQUEST, "포인트 잔액이 부족합니다"),
    ERR_POINT_LOG_NOT_FOUND(HttpStatus.NOT_FOUND, "포인트 로그를 찾을 수 없습니다"),
    ERR_POINT_ALREADY_REFUNDED(HttpStatus.BAD_REQUEST, "이미 환불된 포인트입니다"),
    ERR_POINT_LOCK_FAILED(HttpStatus.CONFLICT, "잠시 후 다시 시도해주세요"),
    ERR_POINT_EVENT_EARN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "포인트 지급에 실패했습니다");

    private final HttpStatus httpStatus;
    private final String message;

    PointExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
