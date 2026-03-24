package one.theone.server.common.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${JWT_KEY:jG5Jco0mSJxvuXjnmgKfPBDFQtWofXIybi1ZqcAcaZw=}")
    private String secretKey;

    @Value("${jwt.secret.accessExpire:3600000}")
    private long accessTokenExpireTime;

    @Value("${jwt.secret.refreshExpire:1209600000}")
    private long refreshTokenExpireTime;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        //jwt.io에서 'BASE64URL ENCODED'를 끄고 성공했다는 것
        //이 키를 디코딩하지 말고 문자열 그대로의 바이트로 써야 한다는 뜻
        byte[] keyBytes = secretKey.trim().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        log.info(">>>> [JWT Key Fix] 문자열 바이트 방식으로 키 로드 완료 (Length: {})", keyBytes.length);
    }

    // Access Token 생성
    public String createAccessToken(Long memberId, String role) {
        log.info(">>>> [Token Generate] memberId: {}, role: {}", memberId, role);
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpireTime))
                .signWith(key) // 여기서 위에서 만든 key를 사용
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpireTime))
                .signWith(key)
                .compact();
    }

    // 토큰에서 회원 PK(memberId) 추출
    public Long getMemberId(String token) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        log.info("[JWT 파싱] 추출된 subject: {}", subject);
        return Long.parseLong(subject);
    }

    // 토큰 유효성 확인
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }
}
