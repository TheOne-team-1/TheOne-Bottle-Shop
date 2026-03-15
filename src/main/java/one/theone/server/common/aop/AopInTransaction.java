package one.theone.server.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 별도의 REQUIRES_NEW 전파 트랜잭션으로 사용되도록 구성
// 락 해제 전 비즈니스 로직의 트랜잭션이 커밋되도록 보장
// 혹여라도 빠트릴 @Transactional 어노테이션에도 작동은 보장 되도록
@Component
public class AopInTransaction {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
