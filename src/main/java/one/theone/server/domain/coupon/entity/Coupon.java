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
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponUseType useType;

    @Column(nullable = false)
    private Long minPrice;

    @Column(nullable = false)
    private Long discountValue;

    @Column(nullable = false)
    private Long availQuantity;

    @Column(nullable = false)
    private Long issuedQuantity;

    @Column(nullable = false)
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Column(nullable = false)
    private Boolean deleted;

    private LocalDateTime deleted_at;

    public static Coupon register(String name, CouponUseType useType, Long minPrice, Long discountValue,
                                  Long availQuantity, LocalDateTime startAt, LocalDateTime endAt) {
        Coupon coupon = new Coupon();
        coupon.name = name;
        coupon.useType = useType;
        coupon.minPrice = minPrice;
        coupon.discountValue = discountValue;
        coupon.availQuantity = availQuantity;
        coupon.issuedQuantity = 0L;
        coupon.startAt = startAt;
        coupon.endAt = endAt;
        coupon.deleted = false;
        return coupon;
    }

    // 발급 가능 여부 검증
    public void validateIssuable() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startAt)) {
            throw new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_NOT_STARTED);
        }

        if (endAt != null && now.isAfter(endAt)) {
            throw new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_EXPIRED);
        }

        if (availQuantity <= issuedQuantity) {
            throw new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_NOT_AVAILABLE);
        }
    }

    // 쿠폰 발급
    public void issueCoupon() {
        validateIssuable();
        this.issuedQuantity++;
    }

    // 할인 금액 계산
    public Long calculateDiscount(Long price) {
        if (price < this.minPrice) {
            throw new ServiceErrorException(CouponExceptionEnum.ERR_COUPON_MIN_PRICE);
        }

        // TODO : 반올림 처리 필요할지도
        return switch (this.useType) {
            case AMOUNT -> Math.min(this.discountValue, price);
            case RATE -> price * this.discountValue / 100;
        };
    }

    public enum CouponUseType {
        AMOUNT, RATE
    }
}
