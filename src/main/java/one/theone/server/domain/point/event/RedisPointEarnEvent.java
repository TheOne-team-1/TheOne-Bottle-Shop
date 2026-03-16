package one.theone.server.domain.point.event;

public record RedisPointEarnEvent(
        Long memberId,
        Long amount,
        String description
) {}
