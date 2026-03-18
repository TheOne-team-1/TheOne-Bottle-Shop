package one.theone.server.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;
import one.theone.server.common.entity.BaseEntity;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // 1상품 1리뷰 원칙
    private Long orderDetailId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 100) // 제목 필수
    private String title;

    @Column(nullable = false, length = 2000) // 내용 필수
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    public static Review create(Long orderDetailId, Long memberId, Long productId,
                                int rating, String title, String content) {
        return Review.builder()
                .orderDetailId(orderDetailId)
                .memberId(memberId)
                .productId(productId)
                .rating(rating)
                .title(title)
                .content(content)
                .deleted(false)
                .build();
    }

    // 소프트 딜리트
    public void delete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    // 권한 체크 로직
    public boolean isNotOwner(Long requesterId) {
        return !this.memberId.equals(requesterId);
    }

    //[좋아요 증가] 동시성 처리를 위해 서비스에서 호출됨
    public void increaseLikeCount() {
        this.likeCount++;
    }

    //[조회수 증가] 어뷰징 체크 통과 시 호출됨
    public void increaseViewCount() {
        this.viewCount++;
    }
}
