package one.theone.server.domain.auth.dto;

//토큰 응답 DTO
public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
