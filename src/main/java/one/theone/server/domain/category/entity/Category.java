package one.theone.server.domain.category.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CategoryExceptionEnum;
import one.theone.server.domain.category.dto.CategoryUpdateRequest;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    private Integer sortNum;

    private Boolean deleted = Boolean.FALSE;
    private LocalDateTime deletedAt;

    public static Category register(
            String name,
            Integer sortNum
    ) {
        Category category = new Category();

        category.name = name;
        category.sortNum = sortNum;

        return category;
    }

    public void updateSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }

    public void update(CategoryUpdateRequest request) {
        if (request.name() != null) {
            this.name = request.name();
        }
        if (request.sortNum() != null) {
            this.sortNum = request.sortNum();
        }
    }

    public void delete() {
        if (this.deleted) {
            throw new ServiceErrorException(CategoryExceptionEnum.ERR_CATEGORY_ALREADY_DELETED);
        }
        this.name = this.name + "_deleted_" + this.id;
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
