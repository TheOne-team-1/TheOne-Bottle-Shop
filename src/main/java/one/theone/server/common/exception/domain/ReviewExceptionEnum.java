package one.theone.server.common.exception.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus; // 이 임포트가 필요합니다.

@Getter
@AllArgsConstructor
public enum ReviewExceptionEnum implements ErrorCode {
    // 각 에러 상황에 맞는 HTTP 상태 코드를 함께 적어줍니다.
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다"),
    NOT_AUTHORIZED_DELETE(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다"),
    ALREADY_REVIEWED(HttpStatus.BAD_REQUEST, "이미 해당 주문에 대한 리뷰를 작성했습니다");

    private final HttpStatus httpStatus; // ErrorCode 규격에 맞춘 필드
    private final String message;

    // 인터페이스에서 요구하는 추상 메서드를 구현합니다.
    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}