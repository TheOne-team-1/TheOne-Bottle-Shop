package one.theone.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "product_category_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategoryDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productCategoryId;

    @Column(nullable = false, length = 100)
    private String name;

    private Integer sortNum;

    public static ProductCategoryDetail register(
            Long productCategoryId,
            String name,
            Integer sortNum
    ) {
        ProductCategoryDetail productCategoryDetail = new ProductCategoryDetail();

        productCategoryDetail.productCategoryId = productCategoryId;
        productCategoryDetail.name = name;
        productCategoryDetail.sortNum = sortNum;

        return productCategoryDetail;
    }
}
