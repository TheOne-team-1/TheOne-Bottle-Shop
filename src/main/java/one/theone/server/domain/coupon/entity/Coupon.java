package one.theone.server.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;

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
        return coupon;
    }

    public enum CouponUseType {
        AMOUNT, RATE
    }
}
