package one.theone.server.domain.coupon.dto.request;

import jakarta.validation.constraints.NotNull;

public record CouponIssueAdminRequest(
        @NotNull(message = "회원 아이디는 필수입니다")
        Long memberId
) {

}
