package one.theone.server.domain.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderCreateFromCartRequest(
        Long memberCouponId,

        @Min(value = 0, message = "사용 포인트는 0 이상이어야 합니다")
        Long usedPoint,

        @NotBlank(message = "주소는 필수입니다")
        @Size(max = 500, message = "주소는 500자 이하여야 합니다")
        String memberAddressSnap,

        @NotBlank(message = "상세 주소는 필수입니다")
        @Size(max = 500, message = "상세 주소는 500자 이하여야 합니다")
        String memberAddressDetailSnap
) {
}
