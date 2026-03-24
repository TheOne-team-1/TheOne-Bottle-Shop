package one.theone.server.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.member.dto.*;
import one.theone.server.domain.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<MemberResponse>> join(@Valid @RequestBody MemberJoinRequest request) {
        MemberResponse response = memberService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success("CREATED", "회원가입 성공", response));
    }

    //관리자 회원가입
    @PostMapping("/signup/admin")
    public BaseResponse<MemberResponse> joinAdmin(@RequestBody @Valid AdminJoinRequest request) {
        MemberResponse response = memberService.joinAdmin(request);
        return BaseResponse.success("OK", "관리자 회원가입이 완료되었습니다.", response);
    }

    //내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<MyInfoResponse>> getMyInfo() {
        Long memberId = getAuthenticatedMemberId();
        MyInfoResponse response = memberService.getMyInfo(memberId);
        return ResponseEntity.ok(BaseResponse.success("OK", "내 정보 조회 성공", response));
    }

    //비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<BaseResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {

        Long memberId = getAuthenticatedMemberId();
        memberService.changePassword(memberId, request);
        return ResponseEntity.ok(BaseResponse.success("OK", "비밀번호 변경 성공", null));
    }

    // 공통 ID 추출 메서드
    private Long getAuthenticatedMemberId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return Long.valueOf(userDetails.getUsername());
            } catch (NumberFormatException e) {
                throw new RuntimeException("인증 객체의 username이 Long 형식이 아닙니다.");
            }
        }
        if (principal instanceof String stringId) {
            return Long.valueOf(stringId);
        }
        if (principal instanceof Long id) {
            return id;
        }
        throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없거나 형식이 올바르지 않습니다.");
    }
}
