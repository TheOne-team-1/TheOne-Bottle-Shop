package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ProductExceptionEnum implements ErrorCode {
    ERR_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
    ERR_PRODUCT_DELETED(HttpStatus.BAD_REQUEST, "삭제된 상품은 수정할 수 없습니다"),
    ERR_PRODUCT_DISCONTINUED(HttpStatus.BAD_REQUEST, "단종된 상품은 수정할 수 없습니다"),
    ERR_PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "재고가 없는 상품은 판매 중으로 변경할 수 없습니다");

    private final HttpStatus httpStatus;
    private final String message;

    ProductExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
