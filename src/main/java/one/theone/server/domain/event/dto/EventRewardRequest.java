package one.theone.server.domain.event.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import one.theone.server.domain.event.entity.EventReward;

public record EventRewardRequest(
        @NotNull(message = "이벤트 보상의 타입은 필수입니다")
        EventReward.EventRewardType rewardType,

        @NotNull(message = "이벤트 보상의 식별번호는 필수입니다")
        @Positive(message = "이벤트 보상의 식별번호는 0보다 커야 합니다")
        Long rewardId // couponId or freebieId
) {
}
