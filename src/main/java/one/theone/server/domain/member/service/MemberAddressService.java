package one.theone.server.domain.member.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.MemberExceptionEnum;
import one.theone.server.domain.member.dto.MemberAddressRequest;
import one.theone.server.domain.member.dto.MemberAddressResponse;
import one.theone.server.domain.member.entity.MemberAddress;
import one.theone.server.domain.member.repository.MemberAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberAddressRepository memberAddressRepository;

    //신규 배송지 등록
    @Transactional
    public MemberAddressResponse registerAddress(Long memberId, MemberAddressRequest request) {
        // 새 주소를 기본 배송지로 설정할 경우 기존 기본 배송지 해제
        if (request.defaultYn()) {
            clearDefaultAddress(memberId, null);
        }

        MemberAddress address = MemberAddress.create(
                memberId,
                request.address(),
                request.addressDetail(),
                request.defaultYn()
        );

        return MemberAddressResponse.from(memberAddressRepository.save(address));
    }

    //특정 회원의 모든 배송지 목록 조회
    @Transactional(readOnly = true)
    public List<MemberAddressResponse> getMemberAddresses(Long memberId) {
        return memberAddressRepository.findAllByMemberId(memberId).stream()
                .map(MemberAddressResponse::from)
                .collect(Collectors.toList());
    }

    //배송지 정보 수정
    //Dirty Checking에 의해 수정일자(updated_at)가 자동으로 업데이트
    @Transactional
    public MemberAddressResponse updateAddress(Long addressId, Long memberId, MemberAddressRequest request) {
        MemberAddress address = memberAddressRepository.findById(addressId)
                .orElseThrow(() -> new ServiceErrorException(MemberExceptionEnum.ERR_ADDRESS_NOT_FOUND));

        // 본인 소유 확인
        validateOwner(address, memberId);

        // 수정하려는 주소를 기본 배송지로 설정할 경우 기존 기본 배송지 해제
        if (request.defaultYn()) {
            clearDefaultAddress(memberId, addressId);
        }

        // 엔티티 업데이트 로직 호출
        address.update(request.address(), request.addressDetail(), request.defaultYn());

        return MemberAddressResponse.from(address);
    }

    //배송지 삭제
    //@SQLDelete 어노테이션에 의해 논리 삭제(Soft Delete)가 수행
    @Transactional
    public void deleteAddress(Long addressId, Long memberId) {
        MemberAddress address = memberAddressRepository.findById(addressId)
                .orElseThrow(() -> new ServiceErrorException(MemberExceptionEnum.ERR_ADDRESS_NOT_FOUND));

        validateOwner(address, memberId);

        // delete 호출 시 실제 삭제가 아닌 삭제 여부와 삭제일자 업데이트 수행
        memberAddressRepository.delete(address);
    }

    //기본 배송지 중복 방지를 위한 해제 로직
    private void clearDefaultAddress(Long memberId, Long excludeAddressId) {
        memberAddressRepository.findByMemberIdAndDefaultYnTrue(memberId)
                .ifPresent(address -> {
                    // 수정 중인 주소와 동일한 경우에는 해제 대상에서 제외
                    if (excludeAddressId == null || !address.getId().equals(excludeAddressId)) {
                        address.updateDefaultStatus(false);
                    }
                });
    }

    //배송지 소유자 검증 로직 - 경어체 메시지 컨벤션 준수
    private void validateOwner(MemberAddress address, Long memberId) {
        if (!address.getMemberId().equals(memberId)) {
            throw new ServiceErrorException(MemberExceptionEnum.ERR_ADDRESS_ACCESS_DENIED);
        }
    }
}
