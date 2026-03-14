package one.theone.server.domain.freebieCategory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.FreebieCategoryExceptionEnum;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryUpdateRequest;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "freebie_categorys")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreebieCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(name = "sort_num")
    private Integer sortNum;

    @Column(nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static FreebieCategory register(String name, Integer sortNum) {
        FreebieCategory freebieCategory = new FreebieCategory();
        freebieCategory.name = name;
        freebieCategory.sortNum = sortNum;
        return freebieCategory;
    }

    public void updateSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }

    public void update(FreebieCategoryUpdateRequest request) {
        if (this.deleted) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_ALREADY_DELETED);
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
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_ALREADY_DELETED);
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
