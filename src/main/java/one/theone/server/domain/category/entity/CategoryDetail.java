package one.theone.server.domain.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CategoryExceptionEnum;
import one.theone.server.domain.category.dto.CategoryDetailUpdateRequest;

import java.time.LocalDateTime;

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

    private Boolean deleted = Boolean.FALSE;
    private LocalDateTime deletedAt;

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

    public void update(CategoryDetailUpdateRequest request) {
        if (this.deleted) {
            throw new ServiceErrorException(CategoryExceptionEnum.ERR_CATEGORY_DETAIL_ALREADY_DELETED);
        }
        if (request.categoryId() != null) {
            this.categoryId = request.categoryId();
        }
        if (request.name() != null) {
            this.name = request.name();
        }
        if (request.sortNum() != null) {
            this.sortNum = request.sortNum();
        }
    }

    public void delete() {
        if (this.deleted) {
            throw new ServiceErrorException(CategoryExceptionEnum.ERR_CATEGORY_DETAIL_ALREADY_DELETED);
        }
        this.name = this.name + "_deleted_" + this.id;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
