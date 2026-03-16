package one.theone.server.domain.coupon.dto.request;

import jakarta.validation.constraints.*;
import one.theone.server.domain.coupon.entity.Coupon;

import java.time.LocalDateTime;

public record CouponCreateRequest(
        @NotBlank(message = "쿠폰명은 필수입니다")
        String name,

        @NotNull(message = "쿠폰 사용 타입은 필수입니다")
        Coupon.CouponUseType useType,

        @NotNull(message = "최소 주문 금액은 필수입니다")
        @Min(value = 10000, message = "최소 주문 금액은 10000원 이상이어야 합니다")
        Long minPrice,

        @NotNull(message = "할인 값은 필수입니다")
        @Min(value = 0, message = "할인 값은 0보다 커야 합니다")
        Long discountValue,

        @NotNull(message = "발급 가능 수량은 필수입니다")
        @Min(value = 0, message = "발급 가능 수량은 0보다 커야 합니다")
        Long availQuantity,

        @NotNull(message = "쿠폰 사용 시작일은 필수입니다")
        LocalDateTime startAt,

        @NotNull(message = "쿠폰 사용 종료일은 필수입니다")
        LocalDateTime endAt
) {}
