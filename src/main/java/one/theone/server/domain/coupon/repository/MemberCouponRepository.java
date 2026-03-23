package one.theone.server.domain.coupon.repository;

import one.theone.server.domain.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    boolean existsByMemberIdAndCouponIdAndStatusAndDeletedFalse(Long memberId, Long couponId, MemberCoupon.MemberCouponStatus status);
    Optional<MemberCoupon> findByIdAndMemberIdAndDeletedFalse(Long id, Long memberId);
    List<MemberCoupon> findAllByCouponIdAndStatusAndDeletedFalse(Long couponId, MemberCoupon.MemberCouponStatus status);
    Optional<MemberCoupon> findByIdAndDeletedFalse(Long id);
    Optional<MemberCoupon> findByMemberIdAndCouponIdAndEventIdAndDeletedFalse(Long memberId, Long couponId, Long eventId);
}
