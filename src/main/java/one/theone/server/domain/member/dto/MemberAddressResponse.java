package one.theone.server.domain.member.dto;

import one.theone.server.domain.member.entity.MemberAddress;

public record MemberAddressResponse(
        Long id,
        Long memberId,
        String address,
        String addressDetail,
        boolean defaultYn
) {
    public static MemberAddressResponse from(MemberAddress address) {
        return new MemberAddressResponse(
                address.getId(),
                address.getMemberId(),
                address.getAddress(),
                address.getAddressDetail(),
                address.isDefaultYn()
        );
    }
}
