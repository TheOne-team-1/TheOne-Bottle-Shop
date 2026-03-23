package one.theone.server.common.exception.domain;

import lombok.Getter;
import one.theone.server.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
public enum ChatExceptionEnum implements ErrorCode {
    ERR_CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다"),
    ERR_CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채팅방 접근 권한이 없습니다"),
    ERR_CHAT_MESSAGE_INVALID(HttpStatus.BAD_REQUEST, "메시지 내용이 올바르지 않습니다"),
    ERR_CHAT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "채팅방 상태값이 올바르지 않습니다"),
    ERR_CHAT_ROOM_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "이미 다른 상담사가 배정된 채팅방입니다"),
    ERR_CHAT_ROOM_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 종료된 채팅입니다");

    private final HttpStatus httpStatus;
    private final String message;

    ChatExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
