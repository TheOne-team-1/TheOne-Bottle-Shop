package one.theone.server.domain.auth.service;

import one.theone.server.common.config.security.JwtProvider;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.auth.dto.LoginRequest;
import one.theone.server.domain.auth.dto.TokenResponse;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.entity.MemberGrade;
import one.theone.server.domain.member.entity.UserRole;
import one.theone.server.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("로그인 성공: 모든 검증을 통과하고 토큰을 발급한다")
    void login_Success() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .passwd("encoded_password")
                .role(UserRole.USER)
                .build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(null); // 차단 안 됨
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtProvider.createAccessToken(anyLong(), anyString())).willReturn("access_token");
        given(jwtProvider.createRefreshToken()).willReturn("refresh_token");

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isEqualTo("access_token");
        assertThat(response.refreshToken()).isEqualTo("refresh_token");

        verify(valueOperations).set(eq("refresh_token:1"), eq("refresh_token"), anyLong(), any(TimeUnit.class));
        verify(redisTemplate).delete(anyString()); // 실패 기록 삭제 확인
    }

    @Test
    @DisplayName("로그인 실패: 비밀번호가 틀리면 실패 횟수가 증가하고 예외가 발생한다")
    void login_InvalidPassword() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "wrong_password");
        Member member = Member.builder().id(1L).passwd("encoded").build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn("0");
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);
        given(valueOperations.increment(anyString())).willReturn(1L);

        // when & then
        assertThrows(ServiceErrorException.class, () -> authService.login(request));
        verify(valueOperations).increment(contains("login_fail:"));
    }

    @Test
    @DisplayName("로그인 실패: 실패 횟수가 3회 이상이면 즉시 차단된다")
    void login_Locked() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "any");
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn("3"); // MAX_FAIL_COUNT 도달

        // when & then
        assertThrows(ServiceErrorException.class, () -> authService.login(request));
        verify(memberRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("토큰 재발급 성공: Redis의 토큰과 일치하면 새 토큰을 발급한다")
    void reissue_Success() {
        // given
        Long memberId = 1L;
        String oldRefreshToken = "old_refresh";
        Member member = Member.builder().id(memberId).role(UserRole.USER).build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("refresh_token:" + memberId)).willReturn(oldRefreshToken);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(jwtProvider.createAccessToken(anyLong(), anyString())).willReturn("new_access");
        given(jwtProvider.createRefreshToken()).willReturn("new_refresh");

        // when
        TokenResponse response = authService.reissue(oldRefreshToken, memberId);

        // then
        assertThat(response.accessToken()).isEqualTo("new_access");
        verify(valueOperations).set(eq("refresh_token:1"), eq("new_refresh"), anyLong(), any(TimeUnit.class));
    }
    @Test
    @DisplayName("토큰 재발급 실패: Redis에 토큰이 없으면 예외 발생")
    void reissue_Fail_TokenNotFound() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(null); // null 케이스

        // when & then
        assertThrows(ServiceErrorException.class, () -> authService.reissue("some_token", 1L));
    }

    @Test
    @DisplayName("로그인 실패 기록: 두 번째 실패부터는 expire를 재설정하지 않는다")
    void handleLoginFail_OnlyExpireOnFirst() {
        // given
        LoginRequest request = new LoginRequest("test@test.com", "wrong");
        Member member = Member.builder().id(1L).passwd("encoded").build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn("1"); // 이미 1번 실패한 상태
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);
        given(valueOperations.increment(anyString())).willReturn(2L); // 두 번째 실패

        // when
        assertThrows(ServiceErrorException.class, () -> authService.login(request));

        // then
        verify(valueOperations).increment(anyString());
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any()); // 1이 아니므로 expire 호출 안 됨
    }

    @Test
    @DisplayName("회원 등급 유지: 금액을 추가해도 등급이 그대로면 등급 변경일은 유지된다")
    void updateGrade_KeepDateIfGradeUnchanged() {
        // given
        Member member = Member.builder()
                .totalPayAmount(10000L) // BRONZE
                .grade(MemberGrade.BRONZE)
                .gradeAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();

        LocalDateTime originalGradeAt = member.getGradeAt();

        // when
        member.addPayAmount(5000L); // 총 15000원 -> 여전히 BRONZE

        // then
        assertThat(member.getGrade()).isEqualTo(MemberGrade.BRONZE);
        assertThat(member.getGradeAt()).isEqualTo(originalGradeAt); // 날짜가 바뀌지 않아야 함
    }

    @Test
    @DisplayName("관리자 생성: ADMIN_ 접두어와 랜덤 코드가 정상 생성된다")
    void createAdmin_CheckPattern() {
        // when
        Member admin = Member.createAdmin("admin@test.com", "pw", "관리자");

        // then
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getRecommendCode()).startsWith("ADMIN_");
        assertThat(admin.getRecommendCode().length()).isGreaterThan(6);
    }
    @Test
    @DisplayName("Redis 연결 체크: 성공과 실패 케이스를 모두 수행하여 catch 블록까지 커버한다")
    void checkRedis_Coverage() {
        // 성공 케이스
        org.springframework.data.redis.connection.RedisConnectionFactory factory = mock(org.springframework.data.redis.connection.RedisConnectionFactory.class);
        org.springframework.data.redis.connection.RedisConnection connection = mock(org.springframework.data.redis.connection.RedisConnection.class);

        given(redisTemplate.getConnectionFactory()).willReturn(factory);
        given(factory.getConnection()).willReturn(connection);
        given(connection.ping()).willReturn("PONG");

        authService.checkRedis(); // 정상 호출 커버

        // 실패 케이스 (catch 블록 커버)
        given(connection.ping()).willThrow(new RuntimeException("Redis Offline"));

        authService.checkRedis(); // 예외 발생 시 catch 블록 커버
    }
    @Test
    @DisplayName("토큰 재발급 실패: 전달된 토큰이 Redis에 저장된 토큰과 다를 때")
    void reissue_Fail_TokenMismatch() {
        // given
        Long memberId = 1L;
        String oldToken = "client_has_this";
        String savedToken = "redis_has_different_one";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(savedToken); // 토큰은 있는데 값이 다름

        // when & then
        assertThrows(ServiceErrorException.class, () -> authService.reissue(oldToken, memberId));
    }
    @Test
    @DisplayName("엔티티 잔여 메서드 커버: 비밀번호 업데이트 및 등급 로직 전수 검사")
    void member_Remaining_Methods_Coverage() {
        // 초기 세팅 (NPE 방지를 위해 totalPayAmount 명시)
        Member member = Member.builder()
                .passwd("old")
                .totalPayAmount(0L)
                .grade(MemberGrade.BRONZE)
                .build();

        member.updatePassword("new_encoded");
        assertThat(member.getPasswd()).isEqualTo("new_encoded");

        // 등급 업데이트 테스트
        member.addPayAmount(10000000L);

        // 검증
        assertThat(member.getGrade()).isNotEqualTo(MemberGrade.BRONZE);
    }
    @Test
    @DisplayName("보안: Redis increment 결과가 null일 때도 에러 없이 통과해야 함")
    void handleLoginFail_WhenIncrementReturnsNull() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(anyString())).willReturn(null); // null 리턴 강제

        // when
        LoginRequest request = new LoginRequest("test@test.com", "wrong");
        Member member = Member.builder().passwd("encoded").build();
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // then
        assertThrows(ServiceErrorException.class, () -> authService.login(request));
    }
    @Test
    @DisplayName("로그인 잠금 체크: Redis에 저장된 count 값이 문자열일 때의 처리")
    void checkLoginLock_StringValue_Coverage() {
        // given
        String failKey = "login_fail:test@test.com";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(failKey)).willReturn("2"); // 문자열 "2" 주입

        given(memberRepository.findByEmail(anyString()))
                .willReturn(Optional.of(Member.builder().passwd("pw").build()));

        // when & then (예외 없이 통과하는지 확인)
        LoginRequest request = new LoginRequest("test@test.com", "wrong");

        // 비밀번호가 틀려서 handleLoginFail까지 가도록 유도
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);
        given(valueOperations.increment(anyString())).willReturn(3L);

        assertThrows(ServiceErrorException.class, () -> authService.login(request));
    }
}