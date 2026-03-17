package one.theone.server.domain.favorite.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import one.theone.server.common.entity.BaseEntity;

@Getter
@Entity
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_favorites_member_product",
                columnNames = {"member_id", "product_id"}
        )
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long productId;

    public static Favorite register(Long memberId, Long productId) {
        Favorite favorite = new Favorite();

        favorite.memberId = memberId;
        favorite.productId = productId;

        return favorite;
    }
}
