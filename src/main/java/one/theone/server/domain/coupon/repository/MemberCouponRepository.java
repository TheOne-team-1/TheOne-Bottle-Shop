package one.theone.server.domain.coupon.repository;

import one.theone.server.domain.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    boolean existsByMemberIdAndCouponIdAndStatus(Long memberId, Long couponId, MemberCoupon.MemberCouponStatus status);
    Optional<MemberCoupon> findByIdAndMemberId(Long id, Long memberId);
}
