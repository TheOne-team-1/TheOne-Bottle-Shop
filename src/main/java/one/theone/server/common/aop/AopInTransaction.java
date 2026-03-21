package one.theone.server.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 별도의 REQUIRES_NEW 전파 트랜잭션으로 사용되도록 구성
// 락 해제 전 비즈니스 로직의 트랜잭션이 커밋되도록 보장
// 사용 서비스엔 @Transacional 어노테이션 삭제 할 것 - 이중 커넥션
@Component
public class AopInTransaction {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
