package one.theone.server.domain.point.event;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.PointExceptionEnum;
import one.theone.server.domain.point.service.PointLockService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PointEarnSubscriber implements MessageListener {

    private final PointLockService pointLockService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte [] pattern) {
        try {
            RedisPointEarnEvent event = objectMapper.readValue(
                    message.getBody(),
                    RedisPointEarnEvent.class
            );

            pointLockService.earnEventPoint(
                    event.memberId(),
                    event.amount(),
                    event.description()
            );
        } catch (Exception e) {
            throw new ServiceErrorException(PointExceptionEnum.ERR_POINT_EVENT_EARN_FAILED);
        }
    }
}
