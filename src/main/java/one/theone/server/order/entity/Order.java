package one.theone.server.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "member_coupon_id")
    private Long memberCouponId;

    @Column(name = "order_num", nullable = false, unique = true, length = 20)
    private String orderNum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "used_point", nullable = false)
    private BigDecimal usedPoint;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "final_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "member_address_snap", nullable = false, length = 500)
    private String memberAddressSnap;

    @Column(name = "member_address_detail_snap", nullable = false, length = 500)
    private String memberAddressDetailSnap;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public static Order create(
            Long memberId, Long memberCouponId, String orderNum, BigDecimal usedPoint,
            BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal finalAmount,
            String memberAddressSnap, String memberAddressDetailSnap) {

        Order order = new Order();

        order.memberId = memberId;
        order.memberCouponId = memberCouponId;
        order.orderNum = orderNum;
        order.usedPoint = usedPoint;
        order.totalAmount = totalAmount;
        order.discountAmount = discountAmount;
        order.finalAmount = finalAmount;
        order.memberAddressSnap = memberAddressSnap;
        order.memberAddressDetailSnap = memberAddressDetailSnap;

        order.status = OrderStatus.PENDING_PAYMENT;

        return order;
    }

    public void addOrderDetail(OrderDetail detail) {
        this.orderDetails.add(detail);
        detail.assignOrder(this);
    }

    public void markCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public void markCompleted() {
        this.status = OrderStatus.COMPLETED;
    }
}
