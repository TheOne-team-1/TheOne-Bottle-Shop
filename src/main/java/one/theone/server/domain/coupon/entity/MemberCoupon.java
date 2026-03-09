package one.theone.server.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "member_coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long couponId;

    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberCouponIssueWay issueWay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberCouponStatus status;

    public enum MemberCouponIssueWay {
        EVENT, ADMIN
    }

    public enum MemberCouponStatus {
        AVAILABLE, USED, EXPIRED
    }
}
