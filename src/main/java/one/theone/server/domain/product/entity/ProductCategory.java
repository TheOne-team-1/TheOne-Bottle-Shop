package one.theone.server.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "product_categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private Integer sortNum;

    public static ProductCategory register(
            String name,
            Integer sortNum
    ) {
        ProductCategory productCategory = new ProductCategory();

        productCategory.name = name;
        productCategory.sortNum = sortNum;

        return productCategory;
    }
}
