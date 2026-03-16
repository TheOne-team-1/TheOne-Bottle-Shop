package one.theone.server.domain.coupon.repository;

import one.theone.server.domain.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
}
