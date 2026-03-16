package one.theone.server.domain.coupon.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.theone.server.domain.coupon.entity.Coupon;
import one.theone.server.domain.coupon.entity.MemberCoupon;
import one.theone.server.domain.coupon.repository.CouponQueryRepository;
import one.theone.server.domain.coupon.repository.CouponRepository;
import one.theone.server.domain.coupon.repository.MemberCouponRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpireScheduler {
    private final CouponQueryRepository couponQueryRepository;

    @Transactional
    @Scheduled(cron = "0 0 5 * * *")
    public void expireCouponSchedule() {
        log.info("쿠폰 만료 스케줄러 작동");
        List<MemberCoupon> expireCouponList = couponQueryRepository.findExpiredMemberCoupons(LocalDateTime.now());

        if(expireCouponList.isEmpty()){
            return;
        }

        log.info("만료 처리 쿠폰 수 : {}", expireCouponList.size());
        expireCouponList.forEach(coupon -> coupon.expireCoupon());
    }
}
