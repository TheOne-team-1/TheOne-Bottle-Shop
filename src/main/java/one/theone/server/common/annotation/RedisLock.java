package one.theone.server.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {
    String key();
    long waitTime() default 5L;
    long leaseTime() default 10L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

// waitTime 을 0 으로 설정해 사용하면 즉시 실패