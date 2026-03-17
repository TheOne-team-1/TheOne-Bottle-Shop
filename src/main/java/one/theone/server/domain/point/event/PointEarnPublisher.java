package one.theone.server.domain.point.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointEarnPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(RedisPointEarnEvent event) {
        String topic = "point-earn";
        redisTemplate.convertAndSend(topic, event);
    }
}
