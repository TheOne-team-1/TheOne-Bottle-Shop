package one.theone.server.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import one.theone.server.domain.member.repository.MemberAddressRepository;
import one.theone.server.domain.member.repository.MemberRecommendLogRepository;
import one.theone.server.domain.member.repository.MemberRepository;
import one.theone.server.domain.point.event.PointEarnPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.beans.factory.annotation.Value;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.MemberExceptionEnum;
import one.theone.server.domain.member.dto.*;
import one.theone.server.domain.member.entity.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;
    @Mock private MemberAddressRepository memberAddressRepository;
    @Mock private MemberRecommendLogRepository memberRecommendLogRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PointEarnPublisher pointEarnPublisher;

    @Value("${admin.signup-key}")
    private String adminSignupKey = "secret-key";

    @BeforeEach
    void setUp() {
        // @Value 어노테이션은 테스트 환경에서 수동 주입이 필요할 수 있습니다.
        ReflectionTestUtils.setField(memberService, "adminSignupKey", "secret-key");
    }

    @Test
    @DisplayName("일반 회원가입 성공 - 추천인 포함")
    void join_Success_WithInviter() {
        // given
        MemberJoinRequest request = new MemberJoinRequest(
                "test@test.com", "pw123", "pw123", "전민우", "20000101",
                "ADDR", "DETAIL" , true,"invitedCode"
        );
        Member inviter = Member.create("inviter@test.com", "hash", "기존회원", "19900101", "INVITE_CODE");
        ReflectionTestUtils.setField(inviter, "id", 100L); // 추천인 ID 설정

        given(memberRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded_pw");
        given(memberRepository.existsByRecommendCode(any())).willReturn(false);

        Member savedMember = Member.create(request.email(), "encoded_pw", request.name(), request.birthAt(), "MYCODE");
        ReflectionTestUtils.setField(savedMember, "id", 1L);
        given(memberRepository.save(any())).willReturn(savedMember);

        given(memberRepository.findByRecommendCode("invitedCode")).willReturn(Optional.of(inviter));

        // when
        memberService.join(request);

        // then
        verify(memberRepository).save(any());
        verify(memberAddressRepository).save(any());
        verify(memberRecommendLogRepository).save(any());
        verify(pointEarnPublisher).publish(any()); // 포인트 발행 확인
    }

    @Test
    @DisplayName("회원가입 실패 - 미성년자 (커버리지 포인트)")
    void join_Fail_Underage() {
        // given: 2026년 기준 2015년생은 미성년자
        MemberJoinRequest request = new MemberJoinRequest(
                "kid@test.com", "pw123", "pw123", "초딩", "20150101",
                "ADDR", "DETAIL" ,true,null
        );

        // when & then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_UNDERAGE.getMessage());
    }

    @Test
    @DisplayName("관리자 가입 실패 - 잘못된 시크릿 키")
    void joinAdmin_Fail_InvalidKey() {
        // given
        AdminJoinRequest request = new AdminJoinRequest(
                "admin@test.com", "pw1", "pw1", "관리자", "wrong-key"
        );

        // when & then
        assertThatThrownBy(() -> memberService.joinAdmin(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_INVALID_ADMIN_KEY.getMessage());
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo_Success() {
        // given
        Long memberId = 1L;
        Member member = Member.create("test@test.com", "pw", "민우", "19950101", "CODE");
        ReflectionTestUtils.setField(member, "id", memberId);

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(memberAddressRepository.findAllByMemberId(memberId)).willReturn(List.of());
        given(memberRecommendLogRepository.findAllByTargetMemberId(memberId)).willReturn(List.of());

        // when
        MyInfoResponse response = memberService.getMyInfo(memberId);

        // then
        assertThat(response.email()).isEqualTo("test@test.com");
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // given
        Long memberId = 1L;
        Member member = Member.create("test@test.com", "old_pw", "민우", "19950101", "CODE");
        PasswordChangeRequest request = new PasswordChangeRequest("old_pw", "new_pw", "new_pw");

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("old_pw", member.getPasswd())).willReturn(true);
        given(passwordEncoder.encode("new_pw")).willReturn("encoded_new_pw");

        // when
        memberService.changePassword(memberId, request);

        // then
        assertThat(member.getPasswd()).isEqualTo("encoded_new_pw");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void join_Fail_DuplicateEmail() {
        // given
        MemberJoinRequest request = new MemberJoinRequest("dup@test.com", "pw", "pw", "이름", "19900101", "주소", "상세", true, null);
        given(memberRepository.existsByEmail(any())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_DUPLICATE_EMAIL.getMessage());
    }
    @Test
    @DisplayName("관리자 가입 성공")
    void joinAdmin_Success() {
        // given
        AdminJoinRequest request = new AdminJoinRequest("admin@test.com", "admin123", "admin123", "관리자", "secret-key");
        given(memberRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded_admin_pw");

        Member admin = Member.createAdmin(request.email(), "encoded_admin_pw", request.name());
        given(memberRepository.save(any())).willReturn(admin);

        // when
        MemberResponse response = memberService.joinAdmin(request);

        // then
        assertThat(response.email()).isEqualTo("admin@test.com");
        verify(memberRepository).save(any());
    }
    @Test
    @DisplayName("추천 실패 - 셀프 추천")
    void join_Fail_SelfRecommend() {
        // given: 가입하려는 사람과 추천인이 같다고 가정 (ID가 1L로 동일)
        MemberJoinRequest request = new MemberJoinRequest("test@test.com", "pw", "pw", "이름", "20000101", "주소", "상세", true, "MYCODE");

        // 가입되는 멤버의 ID를 1L로 세팅
        Member savedMember = Member.create(request.email(), "hash", request.name(), request.birthAt(), "MYCODE");
        ReflectionTestUtils.setField(savedMember, "id", 1L);
        given(memberRepository.save(any())).willReturn(savedMember);

        // 추천인 코드 조회 시 본인(1L)이 반환되도록 설정
        given(memberRepository.findByRecommendCode("MYCODE")).willReturn(Optional.of(savedMember));

        // when & then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_SELF_RECOMMENDATION.getMessage());
    }
    @Test
    @DisplayName("가입 실패 - 비밀번호 불일치")
    void join_Fail_PasswordMismatch() {
        // given: pw123 vs pw456
        MemberJoinRequest request = new MemberJoinRequest("test@test.com", "pw123", "pw456", "이름", "20000101", "주소", "상세", true, null);

        // when & then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_PASSWORD_MISMATCH.getMessage());
    }
    @Test
    @DisplayName("가입 실패 - 비밀번호 확인 불일치")
    void join_Fail_PasswordConfirmMismatch() {
        MemberJoinRequest request = new MemberJoinRequest(
                "test@test.com", "pw123", "different", "민우", "20000101", "주소", "상세", true, null
        );
        // validateJoinRequest의 password 일치 확인 로직을 밟게 됩니다.
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_PASSWORD_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("가입 실패 - 개인정보 동의 미체크")
    void join_Fail_PrivacyPolicy() {
        MemberJoinRequest request = new MemberJoinRequest(
                "test@test.com", "pw1", "pw1", "민우", "20000101", "주소", "상세", false, null
        );
        // privacyPolicyAgreed == false 체크 로직을 밟게 됩니다.
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_PRIVACY_POLICY_AGREED.getMessage());
    }

    @Test
    @DisplayName("추천 실패 - 존재하지 않는 추천인 코드")
    void join_Fail_InvalidRecommendCode() {
        // 1. given
        MemberJoinRequest request = new MemberJoinRequest(
                "test@test.com", "pw1", "pw1", "민우", "20000101", "주소", "상세", true, "WRONG"
        );

        // 가짜 저장 성공 시나리오 (NPE 방지)
        Member savedMember = Member.create(request.email(), "hash", request.name(), request.birthAt(), "MYCODE");
        ReflectionTestUtils.setField(savedMember, "id", 1L);

        given(memberRepository.existsByEmail(any())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("hash");
        given(memberRepository.existsByRecommendCode(any())).willReturn(false);
        given(memberRepository.save(any())).willReturn(savedMember); // <-- 이게 빠져서 NPE가 난 겁니다!

        // 추천인 코드 조회 시 없다고 설정
        given(memberRepository.findByRecommendCode("WRONG")).willReturn(Optional.empty());

        // 2. when & 3. then
        assertThatThrownBy(() -> memberService.join(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(MemberExceptionEnum.ERR_INVALID_RECOMMEND_CODE.getMessage());
    }
}