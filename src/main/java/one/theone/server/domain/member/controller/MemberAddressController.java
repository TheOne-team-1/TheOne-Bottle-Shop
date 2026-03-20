package one.theone.server.domain.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.member.dto.MemberAddressRequest;
import one.theone.server.domain.member.dto.MemberAddressResponse;
import one.theone.server.domain.member.service.MemberAddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class MemberAddressController {

    private final MemberAddressService memberAddressService;

    //신규 배송지 등록
    @PostMapping
    public ResponseEntity<BaseResponse<MemberAddressResponse>> registerAddress(
            @Valid @RequestBody MemberAddressRequest request) {

        Long memberId = getAuthenticatedMemberId();

        MemberAddressResponse response = memberAddressService.registerAddress(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success("CREATED", "배송지 등록 성공", response));
    }

    //나의 모든 배송지 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<List<MemberAddressResponse>>> getMemberAddresses() {

        Long memberId = getAuthenticatedMemberId();

        List<MemberAddressResponse> responses = memberAddressService.getMemberAddresses(memberId);
        return ResponseEntity.ok(BaseResponse.success("OK", "배송지 목록 조회 성공", responses));
    }

    //특정 배송지 정보 수정
    @PatchMapping("/{addressId}")
    public ResponseEntity<BaseResponse<MemberAddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody MemberAddressRequest request) {

        Long memberId = getAuthenticatedMemberId();

        MemberAddressResponse response = memberAddressService.updateAddress(addressId, memberId, request);
        return ResponseEntity.ok(BaseResponse.success("OK", "배송지 정보 수정 성공", response));
    }

    //배송지 삭제 (Soft Delete)
    @DeleteMapping("/{addressId}")
    public ResponseEntity<BaseResponse<Void>> deleteAddress(@PathVariable Long addressId) {

        Long memberId = getAuthenticatedMemberId();

        memberAddressService.deleteAddress(addressId, memberId);
        return ResponseEntity.ok(BaseResponse.success("OK", "배송지 삭제 성공", null));
    }

    private Long getAuthenticatedMemberId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return Long.parseLong(((UserDetails) principal).getUsername());
        } else if (principal instanceof String) {
            return Long.parseLong((String) principal);
        }
        throw new IllegalStateException("인증 정보가 올바르지 않습니다.");
    }
}
