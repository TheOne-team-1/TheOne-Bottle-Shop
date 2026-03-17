package one.theone.server.domain.favorite.repository;

import one.theone.server.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByMemberIdAndProductId(Long memberId, Long productId);

    Optional<Favorite> findByMemberIdAndProductId(Long memberId, Long productId);
}
