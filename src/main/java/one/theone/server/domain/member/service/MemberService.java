package one.theone.server.domain.member.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.MemberExceptionEnum;
import one.theone.server.domain.member.dto.*;
import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.entity.MemberAddress;
import one.theone.server.domain.member.entity.MemberRecommendLog;
import one.theone.server.domain.member.repository.MemberAddressRepository;
import one.theone.server.domain.member.repository.MemberRecommendLogRepository;
import one.theone.server.domain.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final MemberRecommendLogRepository memberRecommendLogRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입 통합 로직
    //성인인증, 추천인코드, 연관관계 없는 로그 저장
    @Transactional
    public MemberResponse join(MemberJoinRequest request) {
        // 1. 기본 검증 (중복 이메일, 비밀번호 일치, 성인 인증)
        validateJoinRequest(request);

        // 2. 비밀번호 암호화 및 본인 추천인 코드 생성
        String encodedPassword = passwordEncoder.encode(request.password());
        String myRecommendCode = generateUniqueRecommendCode();

        // 3. 회원 엔티티 생성 및 저장
        Member member = Member.create(
                request.email(),
                encodedPassword,
                request.name(),
                request.birthAt(),
                myRecommendCode
        );
        Member savedMember = memberRepository.save(member);

        // 4. 주소 저장
        MemberAddress memberAddress = MemberAddress.create(
                savedMember.getId(),
                request.address(),
                request.addressDetail(),
                true
        );

        memberAddressRepository.save(memberAddress);

        // 5. 추천인 코드 입력 시 로그 기록 처리 (ID 직접 참조 방식)
        if (request.invitedCode() != null && !request.invitedCode().isBlank()) {
            processRecommendation(savedMember.getId(), request.invitedCode());
            //pointService.addPoint(inviter.getId(), 500L, "추천인 보상"); 영재님 코드보고 메서드 맞추기
        }

        return MemberResponse.from(savedMember);
    }

    //내 정보 상세 조회 (배송지 목록 포함)
    //패스워드 제외, 등급/추천인코드/주소목록 포함
    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceErrorException(MemberExceptionEnum.ERR_MEMBER_NOT_FOUND));

        // 연관관계 매핑 금지에 따른 직접 조회 방식
        List<MemberAddressResponse> addresses = memberAddressRepository.findAllByMemberId(memberId)
                .stream()
                .map(MemberAddressResponse::from)
                .toList();

        //나를 추천한 사람 수 조회
        //리스트 전체를 가져오는 대신 개수만 세는 게 성능상 유리
        long recommendedCount = memberRecommendLogRepository.findAllByTargetMemberId(memberId).size();

        return MyInfoResponse.from(member, addresses,recommendedCount);
    }

     //비밀번호 변경 로직
     //현재 비번 확인 및 Dirty Checking 활용
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ServiceErrorException(MemberExceptionEnum.ERR_MEMBER_NOT_FOUND));

        // 1. 현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(request.currentPassword(), member.getPasswd())) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_PASSWORD_MISMATCH);
        }

        // 2. 새 비밀번호와 확인용 비밀번호 일치 여부 확인
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_PASSWORD_MISMATCH);
        }

        // 3. 비밀번호 업데이트 (Dirty Checking)
        member.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    //회원가입 유효성 검증
    private void validateJoinRequest(MemberJoinRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_DUPLICATE_EMAIL);
        }

        if (!request.password().equals(request.passwordConfirm())) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_PASSWORD_MISMATCH);
        }

        if (!isAdult(request.birthAt())) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_UNDERAGE);
        }
        if (request.privacyPolicyAgreed()==false){
            throw new ServiceErrorException(MemberExceptionEnum.ERR_PRIVACY_POLICY_AGREED);
        }
    }

    //성인 인증 로직 (만 19세 기준)
    private boolean isAdult(String birthAt) {
        LocalDate birthDate = LocalDate.parse(birthAt, DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDate adultLimitDate = LocalDate.now().minusYears(19);
        return !birthDate.isAfter(adultLimitDate);
    }

    //중복 없는 추천인 코드 생성 (8자리 대문자)
    private String generateUniqueRecommendCode() {
        String code;
        while (true) {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (!memberRepository.existsByRecommendCode(code)) {
                break;
            }
        }
        return code;
    }

    //추천인 로그 저장 로직 (ID 직접 참조)
    private void processRecommendation(Long newMemberId, String invitedCode) {
        //이미 누군가를 추천한 이력이 있는지 확인 (중복 추천 방지)
        if (memberRecommendLogRepository.existsByVoteMemberId(newMemberId)) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_ALREADY_RECOMMENDED);
        }

        Member targetMember = memberRepository.findByRecommendCode(invitedCode)
                .orElseThrow(() -> new ServiceErrorException(MemberExceptionEnum.ERR_INVALID_RECOMMEND_CODE));

        if (targetMember.getId().equals(newMemberId)) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_SELF_RECOMMENDATION);
        }

        MemberRecommendLog log = MemberRecommendLog.create(newMemberId, targetMember.getId());
        memberRecommendLogRepository.save(log);
    }
}
