package one.theone.server.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReissueRequest(
        @NotBlank String refreshToken,
        @NotNull Long memberId
) {}