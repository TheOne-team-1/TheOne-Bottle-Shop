package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum FavoriteExceptionEnum implements ErrorCode {
    ERR_FAVORITE_ALREADY_REGISTER(HttpStatus.CONFLICT, "이미 즐겨찾기에 등록된 상품입니다");

    private final HttpStatus httpStatus;
    private final String message;

    FavoriteExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
