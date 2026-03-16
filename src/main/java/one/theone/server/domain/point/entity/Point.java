package one.theone.server.domain.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "points", uniqueConstraints = {
        @UniqueConstraint(columnNames = "member_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private Long balance;

    private Boolean deleted = Boolean.FALSE;
    private LocalDateTime deletedAt;

    public static Point register(Long memberId) {
        Point point = new Point();

        point.memberId = memberId;
        point.balance = 0L;

        return point;
    }

    public void updateBalance(Long amount) {
        this.balance += amount;
    }
}
