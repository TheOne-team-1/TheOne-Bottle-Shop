package one.theone.server.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import one.theone.server.domain.member.dto.MemberAddressRequest;
import one.theone.server.domain.member.dto.MemberAddressResponse;
import one.theone.server.domain.member.entity.MemberAddress;
import one.theone.server.domain.member.repository.MemberAddressRepository;

@ExtendWith(MockitoExtension.class)
class MemberAddressServiceTest {

    @InjectMocks
    private MemberAddressService memberAddressService;

    @Mock
    private MemberAddressRepository memberAddressRepository;

    @Test
    @DisplayName("배송지 등록 성공")
    void registerAddress_Success() {
        // given
        Long memberId = 1L;
        MemberAddressRequest request = new MemberAddressRequest("새주소", "상세주소", true);
        given(memberAddressRepository.findByMemberIdAndDefaultYnTrue(memberId)).willReturn(Optional.empty());

        MemberAddress savedAddress = MemberAddress.create(memberId, "새주소", "상세주소", true);
        ReflectionTestUtils.setField(savedAddress, "id", 10L);
        given(memberAddressRepository.save(any())).willReturn(savedAddress);

        // when
        MemberAddressResponse response = memberAddressService.registerAddress(memberId, request);

        // then
        assertThat(response.address()).isEqualTo("새주소");
        verify(memberAddressRepository).save(any());
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteAddress_Success() {
        // given
        Long addressId = 10L;
        Long memberId = 1L;
        MemberAddress address = MemberAddress.create(memberId, "주소", "상세", false);
        ReflectionTestUtils.setField(address, "id", addressId);

        given(memberAddressRepository.findById(addressId)).willReturn(Optional.of(address));

        // when
        memberAddressService.deleteAddress(addressId, memberId);

        // then
        verify(memberAddressRepository).delete(address);
    }
    @Test
    @DisplayName("배송지 등록 성공 - 기존 기본 배송지가 있는 경우 해제 확인")
    void registerAddress_Success_ClearExistingDefault() {
        // given
        Long memberId = 1L;
        MemberAddressRequest request = new MemberAddressRequest("새주소", "상세", true);

        // 기존에 이미 기본 배송지가 하나 있다고 가정 (이게 있어야 해제 로직을 탑니다)
        MemberAddress existingDefault = MemberAddress.create(memberId, "옛날주소", "옛날상세", true);
        given(memberAddressRepository.findByMemberIdAndDefaultYnTrue(memberId))
                .willReturn(Optional.of(existingDefault));

        MemberAddress savedAddress = MemberAddress.create(memberId, "새주소", "상세", true);
        ReflectionTestUtils.setField(savedAddress, "id", 11L);
        given(memberAddressRepository.save(any())).willReturn(savedAddress);

        // when
        memberAddressService.registerAddress(memberId, request);

        // then
        assertThat(existingDefault.isDefaultYn()).isFalse(); // 기존꺼가 false로 변했는지 확인
        verify(memberAddressRepository).save(any());
    }
    @Test
    @DisplayName("배송지 삭제 실패 - 본인의 배송지가 아님")
    void deleteAddress_Fail_NotOwner() {
        // given
        Long addressId = 10L;
        Long myId = 1L;
        Long otherId = 99L; // 주인공이 아닌 다른 사람 ID

        MemberAddress othersAddress = MemberAddress.create(otherId, "남의집", "상세", false);
        given(memberAddressRepository.findById(addressId)).willReturn(Optional.of(othersAddress));

        // when & then
        assertThatThrownBy(() -> memberAddressService.deleteAddress(addressId, myId))
                .isInstanceOf(one.theone.server.common.exception.ServiceErrorException.class);
    }
    @Test
    @DisplayName("배송지 삭제 실패 - 존재하지 않는 배송지")
    void deleteAddress_Fail_NotFound() {
        // given
        given(memberAddressRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberAddressService.deleteAddress(999L, 1L))
                .isInstanceOf(one.theone.server.common.exception.ServiceErrorException.class);
    }
    @Test
    @DisplayName("특정 회원의 모든 배송지 목록 조회")
    void getMemberAddresses_Success() {
        // 1. given
        Long memberId = 1L;
        java.util.List<MemberAddress> mockList = java.util.List.of(
                MemberAddress.create(memberId, "서울시 중랑구", "101호", true),
                MemberAddress.create(memberId, "경기도 구리시", "202호", false)
        );

        // 형님 리포지토리에 있는 findAllByMemberId를 사용합니다.
        given(memberAddressRepository.findAllByMemberId(memberId)).willReturn(mockList);

        // 2. when (이름 정확히 getMemberAddresses로 수정했습니다!)
        java.util.List<MemberAddressResponse> responses = memberAddressService.getMemberAddresses(memberId);

        // 3. then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).address()).isEqualTo("서울시 중랑구");
        verify(memberAddressRepository).findAllByMemberId(memberId);
    }

    @Test
    @DisplayName("배송지 수정 성공")
    void updateAddress_Success() {
        // given
        Long addressId = 10L;
        Long memberId = 1L;
        MemberAddress existing = MemberAddress.create(memberId, "옛날주소", "옛날상세", false);
        ReflectionTestUtils.setField(existing, "id", addressId);

        MemberAddressRequest updateRequest = new MemberAddressRequest("새주소", "새상세", false);
        given(memberAddressRepository.findById(addressId)).willReturn(Optional.of(existing));

        // when
        memberAddressService.updateAddress(addressId, memberId, updateRequest);

        // then
        assertThat(existing.getAddress()).isEqualTo("새주소");
        assertThat(existing.getAddressDetail()).isEqualTo("새상세");
    }
}