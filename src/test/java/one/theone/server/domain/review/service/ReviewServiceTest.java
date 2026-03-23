package one.theone.server.domain.review.service;

import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.ReviewExceptionEnum;
import one.theone.server.domain.point.event.PointEarnPublisher;
import one.theone.server.domain.point.event.RedisPointEarnEvent;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.review.dto.*;
import one.theone.server.domain.review.entity.Review;
import one.theone.server.domain.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.Duration;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private PointEarnPublisher pointEarnPublisher;
    @Mock private ProductRepository productRepository;
    @Mock private Product product;
    @Mock private Review review;

    private final Long MEMBER_ID = 1L;
    private final Long PRODUCT_ID = 100L;
    private final Long REVIEW_ID = 500L;

    @Nested
    @DisplayName("리뷰 생성 테스트")
    class CreateReview {
        @Test
        @DisplayName("성공: 새로운 리뷰를 등록하고 포인트를 지급하며 평점을 갱신한다")
        void createReview_Success() {
            // given
            ReviewCreateRequest request = new ReviewCreateRequest(
                    10L,
                    PRODUCT_ID,
                    5,
                    "리뷰 제목",
                    "리뷰 내용입니다."
            );
            given(reviewRepository.existsByOrderDetailIdAndDeletedFalse(anyLong())).willReturn(false);
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(reviewRepository.findAvgRatingByProductId(PRODUCT_ID)).willReturn(Optional.of(new BigDecimal("4.5")));

            // when
            BaseResponse<Void> response = reviewService.createReview(request, MEMBER_ID);

            // then
            assertThat(response.status()).isEqualTo("OK");
            verify(reviewRepository).save(any(Review.class));
            verify(pointEarnPublisher).publish(any(RedisPointEarnEvent.class));
            verify(product).updateRating(any(BigDecimal.class));
        }

        @Test
        @DisplayName("실패: 이미 작성된 리뷰가 있으면 예외가 발생한다")
        void createReview_Fail_AlreadyReviewed() {
            // given
            ReviewCreateRequest request = new ReviewCreateRequest(
                    10L,
                    PRODUCT_ID,
                    5,
                    "리뷰 제목",
                    "리뷰 내용입니다."
            );
            given(reviewRepository.existsByOrderDetailIdAndDeletedFalse(10L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(request, MEMBER_ID))
                    .isInstanceOf(ServiceErrorException.class)
                    .hasMessage(ReviewExceptionEnum.ALREADY_REVIEWED.getMessage());
        }
    }

    @Nested
    @DisplayName("리뷰 상세 조회 테스트 (Redis 조회수 로직 포함)")
    class GetReviewDetail {
        @Test
        @DisplayName("성공: 처음 조회 시 조회수가 증가하고 Redis에 기록된다")
        void getReviewDetail_FirstView() {
            // given
            given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));
            given(redisTemplate.hasKey(anyString())).willReturn(false);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            ReviewDetailResponse detail = new ReviewDetailResponse(
                    REVIEW_ID, "상품명", "작성자", "test@test.com",
                    LocalDateTime.now(), 5, 0, "리뷰 내용"
            );
            given(reviewRepository.findReviewDetail(REVIEW_ID)).willReturn(Optional.of(detail));

            // when
            BaseResponse<ReviewDetailResponse> response = reviewService.getReviewDetail(REVIEW_ID, MEMBER_ID);

            // then
            assertThat(response.data().id()).isEqualTo(REVIEW_ID);
            verify(review).increaseViewCount();
            verify(valueOperations).set(anyString(), anyString(), any(Duration.class));
        }
        @Test
        @DisplayName("목록 조회 성공: Top3 리뷰와 전체 페이징 리뷰를 반환한다")
        void findReviews_Success() {
            // given
            ReviewSearchCondition condition = Mockito.mock(ReviewSearchCondition.class);
            Pageable pageable = PageRequest.of(0, 10);

            List<ReviewResponse> top3 = List.of(Mockito.mock(ReviewResponse.class));
            Page<ReviewResponse> page = new PageImpl<>(List.of(Mockito.mock(ReviewResponse.class)));

            // given 설정
            given(reviewRepository.findTop3ByLikes()).willReturn(top3);
            given(reviewRepository.findReviewsByCondition(any(), any())).willReturn(page);

            // when
            BaseResponse<ReviewListResponse> response = reviewService.findReviews(condition, pageable);

            // then
            assertThat(response.status()).isEqualTo("OK");
            assertThat(response.data().allReviews()).hasSize(1);
            verify(reviewRepository).findTop3ByLikes();
        }

        @Test
        @DisplayName("성공: 24시간 내 재조회 시 조회수가 증가하지 않는다")
        void getReviewDetail_DuplicateView() {
            // given
            given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));
            given(redisTemplate.hasKey(anyString())).willReturn(true);

            ReviewDetailResponse detail = Mockito.mock(ReviewDetailResponse.class);
            given(reviewRepository.findReviewDetail(REVIEW_ID)).willReturn(Optional.of(detail));

            // when
            reviewService.getReviewDetail(REVIEW_ID, MEMBER_ID);

            // then
            verify(review, never()).increaseViewCount();
        }
    }

    @Nested
    @DisplayName("리뷰 삭제 테스트")
    class DeleteReview {
        @Test
        @DisplayName("성공: 본인이 삭제 요청 시 삭제 처리된다")
        void deleteReview_ByOwner() {
            // given
            given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));
            given(review.getMemberId()).willReturn(MEMBER_ID);
            given(review.getProductId()).willReturn(PRODUCT_ID);
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
            given(reviewRepository.findAvgRatingByProductId(PRODUCT_ID)).willReturn(Optional.of(new BigDecimal("4.0")));

            // when
            reviewService.deleteReview(REVIEW_ID, MEMBER_ID, "USER");

            // then
            verify(review).delete();
            verify(product).updateRating(any());
        }

        @Test
        @DisplayName("성공: 관리자가 삭제 요청 시 권한 체크를 통과한다")
        void deleteReview_ByAdmin() {
            // given
            given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));
            given(review.getMemberId()).willReturn(999L); // 다른 사용자
            given(review.getProductId()).willReturn(PRODUCT_ID);
            given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));

            // when
            reviewService.deleteReview(REVIEW_ID, MEMBER_ID, "ADMIN");

            // then
            verify(review).delete();
        }

        @Test
        @DisplayName("실패: 본인도 아니고 관리자도 아니면 권한 에러가 발생한다")
        void deleteReview_Fail_Unauthorized() {
            // given
            given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));
            given(review.getMemberId()).willReturn(999L);

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, MEMBER_ID, "USER"))
                    .isInstanceOf(ServiceErrorException.class)
                    .hasMessage(ReviewExceptionEnum.NOT_AUTHORIZED_DELETE.getMessage());
        }
    }
    @Test
    @DisplayName("좋아요 성공: 리뷰의 좋아요 카운트를 증가시킨다")
    void likeReview_Success() {
        // given
        given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));

        // when
        reviewService.likeReview(REVIEW_ID, MEMBER_ID);

        // then
        verify(review).increaseLikeCount();
    }

    @Test
    @DisplayName("좋아요 실패: 존재하지 않는 리뷰일 경우 예외 발생")
    void likeReview_Fail_NotFound() {
        // given
        given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.likeReview(REVIEW_ID, MEMBER_ID))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(ReviewExceptionEnum.REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상세조회 실패: 존재하지 않는 리뷰일 경우 예외 발생")
    void getReviewDetail_Fail_NotFound() {
        // given
        given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewDetail(REVIEW_ID, MEMBER_ID))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage(ReviewExceptionEnum.REVIEW_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("삭제 실패: 상품 정보를 찾을 수 없을 때 예외 발생 (calculateRating 단계)")
    void deleteReview_Fail_ProductNotFound() {
        // given
        given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));
        given(review.getMemberId()).willReturn(MEMBER_ID);
        given(review.getProductId()).willReturn(PRODUCT_ID);
        given(reviewRepository.findAvgRatingByProductId(PRODUCT_ID)).willReturn(Optional.of(new BigDecimal("4.0")));
        // 상품이 없는 케이스 강제 발생
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(REVIEW_ID, MEMBER_ID, "USER"))
                .isInstanceOf(ServiceErrorException.class);
    }

    @Test
    @DisplayName("삭제 성공: 리뷰 평균 점수가 없을 때(마지막 리뷰 삭제 시) null로 업데이트")
    void deleteReview_Success_WithNoRating() {
        // given
        given(reviewRepository.findByIdAndDeletedFalse(REVIEW_ID)).willReturn(Optional.of(review));
        given(review.getMemberId()).willReturn(MEMBER_ID);
        given(review.getProductId()).willReturn(PRODUCT_ID);
        given(productRepository.findById(PRODUCT_ID)).willReturn(Optional.of(product));
        // 평균 점수가 없는 상황 (Optional.empty)
        given(reviewRepository.findAvgRatingByProductId(PRODUCT_ID)).willReturn(Optional.empty());

        // when
        reviewService.deleteReview(REVIEW_ID, MEMBER_ID, "USER");

        // then
        verify(product).updateRating(null);
    }
}