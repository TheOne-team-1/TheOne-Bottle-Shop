package one.theone.server.common.exception.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthExceptionEnum {
    ERR_UNAUTHORIZED("A001", "인증 정보가 유효하지 않습니다."),
    ERR_FORBIDDEN("A002", "해당 자원에 대한 접근 권한이 없습니다."),
    ERR_EXPIRED_TOKEN("A003", "만료된 토큰입니다."),
    ERR_INVALID_TOKEN("A004", "잘못된 형식의 토큰입니다."),
    ERR_EMPTY_TOKEN("A005", "토큰이 존재하지 않습니다.");

    private final String code;
    private final String message;
}
