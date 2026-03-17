package one.theone.server.domain.favorite.repository;

import one.theone.server.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
