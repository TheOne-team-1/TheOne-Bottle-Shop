package one.theone.server.domain.member.dto;

import one.theone.server.domain.member.entity.Member;
import one.theone.server.domain.member.entity.UserRole;
import java.util.List;


//내 정보 전체 응답 DTO
public record MyInfoResponse(
        Long id,
        String email,
        String name,
        UserRole role,
        String recommendCode,
        long recommendedCount,
        List<MemberAddressResponse> addresses
) {
    public static MyInfoResponse from(Member member,List<MemberAddressResponse> addresses,long recommendedCount) {
        return new MyInfoResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRole(),
                member.getRecommendCode(),
                recommendedCount,
                addresses
        );
    }
}


