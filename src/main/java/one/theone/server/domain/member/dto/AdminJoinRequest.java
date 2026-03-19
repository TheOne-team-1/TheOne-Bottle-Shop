package one.theone.server.domain.member.dto;

public record AdminJoinRequest(
        String email,
        String password,
        String passwordConfirm,
        String name,
        String adminKey // 여기에 "THE_ONE_ADMIN_SECRET_2026" 관리자회원가입 인증키를 넣어야 회원가입 진행
) {}
