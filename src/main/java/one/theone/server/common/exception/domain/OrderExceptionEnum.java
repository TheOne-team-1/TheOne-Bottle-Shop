package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderExceptionEnum implements ErrorCode {

    ERR_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다"),
    ERR_ORDER_ITEM_EMPTY(HttpStatus.BAD_REQUEST, "주문 상품이 없습니다"),
    ERR_ORDER_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "상품 수량은 1 이상이어야 합니다"),
    ERR_ORDER_INVALID_PRICE(HttpStatus.BAD_REQUEST, "상품 가격은 0보다 커야합니다"),
    ERR_ORDER_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "최종 주문 금액은 0보다 작을 수 없습니다"),
    ERR_ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다");

    private final HttpStatus httpStatus;
    private final String message;

    OrderExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
