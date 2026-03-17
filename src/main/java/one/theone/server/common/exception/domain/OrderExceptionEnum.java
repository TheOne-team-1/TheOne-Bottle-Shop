package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderExceptionEnum implements ErrorCode {

    ERR_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다"),
    ERR_ORDER_ITEM_NOT_FOUND(HttpStatus.BAD_REQUEST, "주문 상품을 찾을 수 없습니다"),
    ERR_ORDER_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "상품 수량이 올바르지 않습니다"),
    ERR_ORDER_INVALID_PRICE(HttpStatus.BAD_REQUEST, "상품 가격이 올바르지 않습니다"),
    ERR_ORDER_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "주문 금액이 올바르지 않습니다"),
    ERR_ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다"),
    ERR_ORDER_STOCK_EXCEEDED(HttpStatus.BAD_REQUEST, "재고를 초과한 주문입니다."),
    ERR_ORDER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "주문 취소가 가능한 상태가 아닙니다"),
    ERR_ORDER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주문번호 생성에 실패했습니다");

    private final HttpStatus httpStatus;
    private final String message;

    OrderExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
