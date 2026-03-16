package one.theone.server.domain.freebieCategory.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.FreebieCategoryExceptionEnum;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryDetailUpdateRequest;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "freebie_category_details")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FreebieCategoryDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "freebie_category_id")
    private Long freebieCategoryId;

    @Column(length = 100)
    private String name;

    @Column(name = "sort_num")
    private Integer sortNum;

    @Column(nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static FreebieCategoryDetail register(Long freebieCategoryId, String name, Integer sortNum) {
        FreebieCategoryDetail detail = new FreebieCategoryDetail();
        detail.freebieCategoryId = freebieCategoryId;
        detail.name = name;
        detail.sortNum = sortNum;
        return detail;
    }

    public void updateSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }

    public void update(FreebieCategoryDetailUpdateRequest request) {
        if (this.deleted) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_DETAIL_ALREADY_DELETED);
        }
        if (request.freebieCategoryId() != null) {
            this.freebieCategoryId = request.freebieCategoryId();
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
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_DETAIL_ALREADY_DELETED);
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
