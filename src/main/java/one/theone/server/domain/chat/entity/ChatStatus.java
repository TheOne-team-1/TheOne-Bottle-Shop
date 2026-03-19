package one.theone.server.domain.chat.entity;

public enum ChatStatus {
    WAITING,      // 대기중 (고객 문의 직후)
    IN_PROGRESS,  // 처리중 (판매자/관리자 답변 중)
    COMPLETED     // 완료 (상담 종료)
}