package one.theone.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(nullable = false, precision = 5, scale = 3)
    private BigDecimal abv;

    @Column(nullable = false)
    private int volumeMl;

    @Column(nullable = false)
    private Long productCategoryDetailId;

    @Column(nullable = false)
    private Long quantity;

    public static Product register(
            String name,
            Long price,
            BigDecimal abv,
            int volumeMl,
            Long productCategoryDetailId,
            Long quantity
    ) {
        Product product = new Product();

        product.name = name;
        product.price = price;
        product.status = ProductStatus.SALES;
        product.abv = abv;
        product.volumeMl = volumeMl;
        product.productCategoryDetailId = productCategoryDetailId;
        product.quantity = quantity;

        return product;
    }

    public enum ProductStatus {
        SALES, SOLD_OUT, DISCONTINUE
    }
}
