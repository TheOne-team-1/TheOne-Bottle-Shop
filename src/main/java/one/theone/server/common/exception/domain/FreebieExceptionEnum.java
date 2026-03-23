package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum FreebieExceptionEnum implements ErrorCode {
    ERR_FREEBIE_NOT_FOUND(HttpStatus.NOT_FOUND, "사은품을 찾을 수 없습니다")
    , ERR_FREEBIE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 사은품입니다")
    , ERR_FREEBIE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "제공 불가 상태의 사은품입니다")
    , ERR_FREEBIE_INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "사은품 재고가 부족합니다")
    , ERR_FREEBIE_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "사은품 수량은 1 이상이어야 합니다")
    , ERR_FREEBIE_LOCK_FAILED(HttpStatus.CONFLICT, "잠시 후 다시 시도해주세요");

    private final HttpStatus httpStatus;
    private final String message;

    FreebieExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
