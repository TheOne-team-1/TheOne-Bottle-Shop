package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum CartExceptionEnum implements ErrorCode {
    ERR_CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 해당 상품이 존재하지 않습니다"),
    ERR_CART_INVALID_PRODUCT_ID(HttpStatus.BAD_REQUEST, "상품 ID가 올바르지 않습니다"),
    ERR_CART_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "상품 수량은 0보다 작을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;

    CartExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
