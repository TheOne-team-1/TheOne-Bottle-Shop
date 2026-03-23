package one.theone.server.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

@Getter
@Entity
@Table(
        name = "order_details",
        indexes = {
                @Index(name = "idx_order_details_order_id", columnList = "order_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name_snap", nullable = false, length = 255)
    private String productNameSnap;

    @Column(name = "product_price_snap", nullable = false)
    private Long productPriceSnap;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "line_amount", nullable = false)
    private Long lineAmount;

    public static OrderDetail create(
            Long orderId,
            Long productId,
            String productNameSnap,
            Long productPriceSnap,
            Integer quantity
    ) {
        OrderDetail detail = new OrderDetail();

        detail.orderId = orderId;
        detail.productId = productId;
        detail.productNameSnap = productNameSnap;
        detail.productPriceSnap = productPriceSnap;
        detail.quantity = quantity;
        detail.lineAmount = productPriceSnap * quantity;

        return detail;
    }
}
