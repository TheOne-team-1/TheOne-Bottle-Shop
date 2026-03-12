package one.theone.server.domain.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductCreateRequest(
        @NotBlank(message = "상품명은 필수입니다")
        String name,

        @NotNull(message = "가격은 필수입니다")
        @Positive(message = "가격은 0보다 커야 합니다")
        Long price,

        @NotNull(message = "도수는 필수입니다")
        @DecimalMin(value = "0.000", message = "도수는 0.000 이상이어야 합니다")
        @DecimalMax(value = "99.999", message = "도수는 99.999 이하여야 합니다")
        BigDecimal abv,

        @Positive(message = "용량은 0보다 커야 합니다")
        int volumeMl,

        @NotNull(message = "카테고리는 필수입니다")
        Long productCategoryDetailId,

        @NotNull(message = "재고 수량은 필수입니다")
        @PositiveOrZero(message = "재고 수량은 0 이상이어야 합니다")
        Long quantity
) {}
