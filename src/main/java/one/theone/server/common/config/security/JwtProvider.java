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

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.secret.accessExpire:3600000}")
    private long accessTokenExpireTime;

    @Value("${jwt.secret.refreshExpire:1209600000}")
    private long refreshTokenExpireTime;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        // 만약 yml의 키가 Base64 인코딩 상태로 입력시 아래 로직으로 변경
        // Base64.getDecoder().decode(secretKey)
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    //Access Token 생성
    public String createAccessToken(Long memberId, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("role", role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpireTime))
                .signWith(key)
                .compact();
    }

    //Refresh Token 생성
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpireTime))
                .signWith(key)
                .compact();
    }

    //토큰에서 회원 PK(memberId) 추출
    public Long getMemberId(String token) {
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(subject);
    }

    //토큰 유효성 확인
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
