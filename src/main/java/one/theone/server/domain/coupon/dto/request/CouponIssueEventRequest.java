package one.theone.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotNull;

public record CouponIssueEventRequest(
        @NotNull(message = "이벤트 아이디는 필수입니다")
        Long eventId
) {
}
