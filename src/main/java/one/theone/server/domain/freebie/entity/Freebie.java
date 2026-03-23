package one.theone.server.domain.freebie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.FreebieExceptionEnum;
import one.theone.server.domain.freebie.dto.request.FreebieUpdateRequest;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "freebies")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Freebie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "freebie_category_detail_id", nullable = false)
    private Long freebieCategoryDetailId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FreebieStatus status;

    @Column(nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum FreebieStatus {
        AVAILABLE, NO_AVAILABLE
    }

    public static Freebie register(Long freebieCategoryDetailId, String name, Long quantity) {
        Freebie freebie = new Freebie();
        freebie.freebieCategoryDetailId = freebieCategoryDetailId;
        freebie.name = name;
        freebie.quantity = quantity;
        freebie.status = quantity > 0 ? FreebieStatus.AVAILABLE : FreebieStatus.NO_AVAILABLE;
        return freebie;
    }

    public void update(FreebieUpdateRequest request) {
        if (this.deleted) {
            throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_ALREADY_DELETED);
        }

        if (request.name() != null) {
            this.name = request.name();
        }

        if (request.quantity() != null) {
            this.quantity = request.quantity();
            this.status = request.quantity() > 0 ? FreebieStatus.AVAILABLE : FreebieStatus.NO_AVAILABLE;
        }

        if (request.freebieCategoryDetailId() != null) {
            this.freebieCategoryDetailId = request.freebieCategoryDetailId();
        }
    }

    public void delete() {
        if (this.deleted) {
            throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_ALREADY_DELETED);
        }

        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void decreaseStock(Long quantity) {
        if (quantity <= 0) {
            throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_INVALID_QUANTITY);
        }

        if (this.status != FreebieStatus.AVAILABLE || this.deleted) {
            throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_NOT_AVAILABLE);
        }

        if (this.quantity < quantity) {
            throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_INSUFFICIENT_STOCK);
        }

        this.quantity -= quantity;
        if (this.quantity == 0) {
            this.status = FreebieStatus.NO_AVAILABLE;
        }
    }

    public void increaseStock(Long quantity) {
        if (quantity <= 0) {
            throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_INVALID_QUANTITY);
        }

        this.quantity += quantity;
        if (this.status == FreebieStatus.NO_AVAILABLE) {
            this.status = FreebieStatus.AVAILABLE;
        }
    }
}
