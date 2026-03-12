package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum CategoryExceptionEnum implements ErrorCode {
    ERR_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "대분류 카테고리를 찾을 수 없습니다"),
    ERR_DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "이미 존재하는 대분류 카테고리입니다"),
    ERR_CATEGORY_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "소분류 카테고리를 찾을 수 없습니다"),
    ERR_DUPLICATE_CATEGORY_DETAIL_NAME(HttpStatus.CONFLICT, "대분류 카테고리에 이미 존재하는 소분류 카테고리입니다");

    private final HttpStatus httpStatus;
    private final String message;

    CategoryExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}