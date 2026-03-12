package one.theone.server.domain.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "category_details", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_category_details_category_id_name",
                columnNames = {"category_id", "name"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryDetail extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 100)
    private String name;

    private Integer sortNum;

    public static CategoryDetail register(
            Long categoryId,
            String name,
            Integer sortNum
    ) {
        CategoryDetail categoryDetail = new CategoryDetail();

        categoryDetail.categoryId = categoryId;
        categoryDetail.name = name;
        categoryDetail.sortNum = sortNum;

        return categoryDetail;
    }

    public void updateSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }
}
