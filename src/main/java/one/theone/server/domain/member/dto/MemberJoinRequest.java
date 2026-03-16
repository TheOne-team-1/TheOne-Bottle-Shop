package one.theone.server.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record MemberJoinRequest(
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String passwordConfirm,
        @NotBlank String name,
        @NotBlank String birthAt,
        @NotBlank String address,
        String addressDetail,
        @NotNull Boolean privacyPolicyAgreed,
        String invitedCode
) {}
