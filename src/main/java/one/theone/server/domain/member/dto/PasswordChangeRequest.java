package one.theone.server.domain.member.dto;

//비밀번호 변경 요청 DTO
public record PasswordChangeRequest(
        String currentPassword,
        String newPassword,
        String newPasswordConfirm
) {
}
