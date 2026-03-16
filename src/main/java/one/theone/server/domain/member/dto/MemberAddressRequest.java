package one.theone.server.domain.member.dto;

import jakarta.validation.constraints.NotBlank;


public record MemberAddressRequest(
        @NotBlank String address,
        @NotBlank String addressDetail,
        boolean defaultYn
) {}

