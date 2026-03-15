package one.theone.server.domain.product.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        String name,

        @Positive(message = "가격은 0보다 커야 합니다")
        Long price,

        @DecimalMin(value = "0.000", message = "도수는 0.000 이상이어야 합니다")
        @DecimalMax(value = "99.999", message = "도수는 99.999 이하여야 합니다")
        BigDecimal abv,

        @Positive(message = "용량은 0보다 커야 합니다")
        Integer volumeMl,

        Long categoryDetailId,

        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다")
        Long quantity
) {}
