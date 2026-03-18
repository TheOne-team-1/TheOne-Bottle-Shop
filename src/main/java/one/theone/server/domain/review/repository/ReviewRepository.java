package one.theone.server.domain.review.repository;

import one.theone.server.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {
    // Soft Delete 대응: 삭제되지 않은 리뷰만 ID로 조회
    Optional<Review> findByIdAndDeletedFalse(Long id);

    //1상품 1리뷰 중복 체크
    boolean existsByOrderDetailIdAndDeletedFalse(Long orderDetailId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.deleted = false")
    Optional<BigDecimal> findAvgRatingByProductId(@Param("productId") Long productId);
}
