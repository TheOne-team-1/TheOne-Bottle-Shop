package one.theone.server.domain.member.dto;

import one.theone.server.domain.member.entity.Member;

public record MemberResponse(
        Long id,
        String email,
        String name,
        String recommendCode
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getRecommendCode()
        );
    }
}
