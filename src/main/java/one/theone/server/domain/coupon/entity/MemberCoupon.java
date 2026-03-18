package one.theone.server.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CouponExceptionEnum;

import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deleted_at;

    // 이벤트 참여 발급
    public static MemberCoupon issuedByEvent(Long memberId, Long couponId, Long eventId) {
        MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.memberId = memberId;
        memberCoupon.couponId = couponId;
        memberCoupon.eventId = eventId;
        memberCoupon.issueWay = MemberCouponIssueWay.EVENT;
        memberCoupon.status = MemberCouponStatus.AVAILABLE;
        memberCoupon.deleted = false;
        return memberCoupon;
    }

    // 관리자 발급
    public static MemberCoupon issuedByAdmin(Long memberId, Long couponId) {
        MemberCoupon memberCoupon = new MemberCoupon();
        memberCoupon.memberId = memberId;
        memberCoupon.couponId = couponId;
        memberCoupon.issueWay = MemberCouponIssueWay.ADMIN;
        memberCoupon.status = MemberCouponStatus.AVAILABLE;
        memberCoupon.deleted = false;
        return memberCoupon;
    }

    // 관리자 회수
    public void recallByAdmin() {
        if (this.status == MemberCouponStatus.RECALL) {
            throw new ServiceErrorException(CouponExceptionEnum.ERR_MEMBER_COUPON_ALREADY_RECALLED);
        }

        this.status = MemberCouponStatus.RECALL;
    }

    public void delete() {
        this.deleted = true;
        this.deleted_at = LocalDateTime.now();
    }

    // 사용 처리
    public void useCoupon() {
        if (this.status != MemberCouponStatus.AVAILABLE) {
            throw new ServiceErrorException(CouponExceptionEnum.ERR_MEMBER_COUPON_NOT_AVAILABLE);
        }

        this.status = MemberCouponStatus.USED;
    }

    // 만료 처리
    public void expireCoupon() {
        if (this.status != MemberCouponStatus.AVAILABLE) {
            return;
        }

        this.status = MemberCouponStatus.EXPIRED;
    }

    // 환불 시 복구
    public void refundCoupon(LocalDateTime endAt) {
        if (LocalDateTime.now().isBefore(endAt)) {
            this.status = MemberCouponStatus.AVAILABLE;
        } else {
            this.status = MemberCouponStatus.EXPIRED;
        }
    }

    // 상태 확인
    public boolean isAvailable() {
        return this.status == MemberCouponStatus.AVAILABLE;
    }

    public enum MemberCouponIssueWay {
        EVENT, ADMIN
    }

    public enum MemberCouponStatus {
        AVAILABLE, USED, EXPIRED, RECALL
    }
}
