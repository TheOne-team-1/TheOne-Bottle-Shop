package one.theone.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ProductExceptionEnum;
import one.theone.server.domain.product.dto.ProductStatusUpdateRequest;
import one.theone.server.domain.product.dto.ProductUpdateRequest;

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
    private Long categoryDetailId;

    @Column(nullable = false)
    private Long quantity;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    public static Product register(
            String name,
            Long price,
            BigDecimal abv,
            int volumeMl,
            Long categoryDetailId,
            Long quantity
    ) {
        Product product = new Product();

        product.name = name;
        product.price = price;
        product.status = quantity == 0 ? ProductStatus.SOLD_OUT : ProductStatus.SALES;
        product.abv = abv;
        product.volumeMl = volumeMl;
        product.categoryDetailId = categoryDetailId;
        product.quantity = quantity;

        return product;
    }

    public enum ProductStatus {
        SALES, SOLD_OUT, DISCONTINUE
    }

    public void update(ProductUpdateRequest request) {
        if (request.name() != null && !request.name().isBlank()) {
            this.name = request.name();
        }
        if (request.price() != null) {
            this.price = request.price();
        }
        if (request.abv() != null) {
            this.abv = request.abv();
        }
        if (request.volumeMl() != 0) {
            this.volumeMl = request.volumeMl();
        }
        if (request.categoryDetailId() != null) {
            this.categoryDetailId = request.categoryDetailId();
        }
        if (request.quantity() != null) {
            this.quantity = request.quantity();
            this.status = request.quantity() == 0 ? ProductStatus.SOLD_OUT : ProductStatus.SALES;
        }
    }

    public void updateStatus(ProductStatusUpdateRequest request) {
        if (this.getDeleted()) {
            throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_DELETED);
        }
        if (this.status == ProductStatus.DISCONTINUE) {
            throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_DISCONTINUED);
        }
        if (this.quantity == 0 && request.status() == ProductStatus.SALES) {
            throw new ServiceErrorException(ProductExceptionEnum.ERR_PRODUCT_OUT_OF_STOCK);
        }
        this.status = request.status();
    }
}
