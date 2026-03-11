package one.theone.server.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "order_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name_snap", nullable = false, length = 255)
    private String productNameSnap;

    @Column(name = "product_price_snap", nullable = false, precision = 15, scale = 2)
    private BigDecimal productPriceSnap;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal lineAmount;

    public static OrderDetail create(
            Long productId,
            String productNameSnap,
            BigDecimal productPriceSnap,
            Integer quantity
    ) {
        OrderDetail detail = new OrderDetail();

        detail.productId = productId;
        detail.productNameSnap = productNameSnap;
        detail.productPriceSnap = productPriceSnap;
        detail.quantity = quantity;
        detail.lineAmount = productPriceSnap.multiply(BigDecimal.valueOf(quantity));

        return detail;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}
