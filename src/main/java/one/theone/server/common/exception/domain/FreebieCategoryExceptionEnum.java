package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum FreebieCategoryExceptionEnum implements ErrorCode {
    ERR_FREEBIE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "사은품 카테고리를 찾을 수 없습니다"),
    ERR_FREEBIE_CATEGORY_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 사은품 카테고리입니다"),
    ERR_FREEBIE_CATEGORY_HAS_DETAILS(HttpStatus.BAD_REQUEST, "하위 세부 카테고리가 존재하여 삭제할 수 없습니다"),
    ERR_DUPLICATE_FREEBIE_CATEGORY_NAME(HttpStatus.CONFLICT, "이미 존재하는 사은품 카테고리 이름입니다"),
    ERR_FREEBIE_CATEGORY_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "사은품 세부 카테고리를 찾을 수 없습니다"),
    ERR_FREEBIE_CATEGORY_DETAIL_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 사은품 세부 카테고리입니다"),
    ERR_FREEBIE_CATEGORY_DETAIL_HAS_FREEBIE(HttpStatus.BAD_REQUEST, "해당 세부 카테고리에 사은품이 존재하여 삭제할 수 없습니다"),
    ERR_DUPLICATE_FREEBIE_CATEGORY_DETAIL_NAME(HttpStatus.CONFLICT, "이미 존재하는 사은품 세부 카테고리 이름입니다");

    private final HttpStatus httpStatus;
    private final String message;

    FreebieCategoryExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
