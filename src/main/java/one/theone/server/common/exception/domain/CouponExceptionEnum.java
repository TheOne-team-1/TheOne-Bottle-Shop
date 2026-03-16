package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum CouponExceptionEnum implements ErrorCode {
    ERR_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다")
    , ERR_COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "발급 가능한 쿠폰이 없습니다")
    , ERR_COUPON_NOT_STARTED(HttpStatus.BAD_REQUEST, "아직 발급 가능한 기간이 아닙니다")
    , ERR_COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 쿠폰입니다")
    , ERR_COUPON_MIN_PRICE(HttpStatus.BAD_REQUEST, "쿠폰 최소 주문금액을 충족하지 못했습니다")
    , ERR_COUPON_INVALID_RATE(HttpStatus.BAD_REQUEST, "할인율은 1 이상 100 이하여야 합니다")
    , ERR_MEMBER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 쿠폰을 찾을 수 없습니다")
    , ERR_MEMBER_COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "사용 가능한 상태의 쿠폰이 아닙니다")
    , ERR_MEMBER_COUPON_ALREADY_RECALLED(HttpStatus.BAD_REQUEST, "이미 회수된 쿠폰입니다")
    , ERR_COUPON_LOCK_FAILED(HttpStatus.CONFLICT, "잠시 후 다시 시도해주세요");

    private final HttpStatus httpStatus;
    private final String message;

    CouponExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
