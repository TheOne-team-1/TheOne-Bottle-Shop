package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberExceptionEnum implements ErrorCode {
    ERR_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다"),
    ERR_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 배송지 정보입니다"),
    ERR_ADDRESS_ACCESS_DENIED(HttpStatus.FORBIDDEN, "배송지에 대한 접근 권한이 없습니다"),
    ERR_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 사용 중인 이메일입니다"),
    ERR_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다"),
    ERR_UNDERAGE(HttpStatus.BAD_REQUEST, "만 19세 미만의 미성년자는 가입할 수 없습니다"),
    ERR_INVALID_RECOMMEND_CODE(HttpStatus.BAD_REQUEST, "존재하지 않는 추천인 코드입니다"),
    ERR_SELF_RECOMMENDATION(HttpStatus.BAD_REQUEST, "자기 자신은 추천할 수 없습니다"),
    ERR_ALREADY_RECOMMENDED(HttpStatus.BAD_REQUEST, "이미 추천인 등록을 완료하셨습니다"),
    ERR_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다"),
    ERR_LOGIN_LOCKED(HttpStatus.FORBIDDEN, "로그인 3회 실패로 인해 30초간 접속이 제한됩니다"),
    ERR_UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    ERR_INVALID_ADMIN_KEY(HttpStatus.FORBIDDEN, "시크릿 코드가 일치하지 않습니다."),
    ERR_PRIVACY_POLICY_AGREED(HttpStatus.BAD_REQUEST, "개인정보 동의를 해야 회원가입 진행이 가능합니다");

    private final HttpStatus httpStatus;
    private final String message;

    MemberExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
