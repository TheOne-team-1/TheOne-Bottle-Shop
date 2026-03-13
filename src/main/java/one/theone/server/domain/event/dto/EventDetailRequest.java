package one.theone.server.domain.event.dto;

import jakarta.validation.constraints.Positive;

public record EventDetailRequest(
        @Positive(message = "이벤트 조건 상품의 식별번호는 0보다 커야 합니다")
        Long eventProductId,

        @Positive(message = "이벤트 조건 금액은 0보다 커야 합니다")
        Long minPrice
) {
}
