package one.theone.server.domain.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.config.security.JwtProvider;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.MemberExceptionEnum;
import one.theone.server.domain.auth.dto.LoginRequest;
import one.theone.server.domain.auth.dto.TokenResponse;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.repository.MemberRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtProvider jwtProvider;

    private static final String FAIL_KEY_PREFIX = "login_fail:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final int MAX_FAIL_COUNT = 3;
    private static final long LOCK_TIME = 30; // 30초 차단

    @PostConstruct
    public void checkRedis() {
        try {
            // "PONG"이라는 대답이 오면 연결 성공
            String result = redisTemplate.getConnectionFactory().getConnection().ping();
            System.out.println("*** 레디스 연결 상태: " + result);
        } catch (Exception e) {
            System.err.println("*** 레디스 연결 실패: " + e.getMessage());
        }
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = request.email();
        String failKey = FAIL_KEY_PREFIX + email;

        // 1. 차단 여부 확인
        checkLoginLock(failKey);

        // 2. 회원 존재 여부 확인
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    handleLoginFail(failKey); // 회원 없어도 실패 횟수 증가 (보안)
                    return new ServiceErrorException(MemberExceptionEnum.ERR_INVALID_PASSWORD);
                });

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), member.getPasswd())) {
            handleLoginFail(failKey);
            throw new ServiceErrorException(MemberExceptionEnum.ERR_INVALID_PASSWORD);
        }

        // 4. 로그인 성공 시 처리
        redisTemplate.delete(failKey); // 실패 기록 초기화

        // 토큰 발급 (Previously provided information reflected: memberId, role)
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken();

        // 5. Refresh Token Redis 저장 (7일간 유지)
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + member.getId(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    private void checkLoginLock(String failKey) {
        Object countObj = redisTemplate.opsForValue().get(failKey);
        if (countObj != null) {
            int count = Integer.parseInt(countObj.toString());
            if (count >= MAX_FAIL_COUNT) {
                throw new ServiceErrorException(MemberExceptionEnum.ERR_LOGIN_LOCKED);
            }
        }
    }

    private void handleLoginFail(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, LOCK_TIME, TimeUnit.SECONDS);
        }
    }
}
