package one.theone.server.domain.review.controller;

import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.review.dto.ReviewDetailResponse;
import one.theone.server.domain.review.dto.ReviewListResponse;
import one.theone.server.domain.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @WithMockUser
    @DisplayName("리뷰 생성: 200 OK")
    void createReview_Success() throws Exception {
        given(reviewService.createReview(any(), any()))
                .willReturn(BaseResponse.success("OK", "리뷰 작성 완료", null));

        String content = """
                {
                    "orderDetailId": 10,
                    "productId": 100,
                    "rating": 5,
                    "title": "제목",
                    "content": "내용"
                }
                """;

        mockMvc.perform(post("/api/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 목록 조회: 200 OK")
    void listReviews_Success() throws Exception {
        ReviewListResponse response = Mockito.mock(ReviewListResponse.class);
        given(reviewService.findReviews(any(), any()))
                .willReturn(BaseResponse.success("OK", "조회 성공", response));

        mockMvc.perform(get("/api/review")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 상세 조회: 200 OK")
    void getReviewDetail_Success() throws Exception {
        ReviewDetailResponse response = Mockito.mock(ReviewDetailResponse.class);
        given(reviewService.getReviewDetail(anyLong(), any()))
                .willReturn(BaseResponse.success("OK", "조회 성공", response));

        mockMvc.perform(get("/api/review/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 좋아요: 200 OK")
    void likeReview_Success() throws Exception {
        given(reviewService.likeReview(anyLong(), any()))
                .willReturn(BaseResponse.success("OK", "좋아요 성공", null));

        mockMvc.perform(post("/api/review/1/like")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("좋아요 성공"));
    }

    @Test
    @WithMockUser
    @DisplayName("리뷰 삭제: 200 OK")
    void deleteReview_Success() throws Exception {
        given(reviewService.deleteReview(anyLong(), any(), any()))
                .willReturn(BaseResponse.success("OK", "삭제 성공", null));

        mockMvc.perform(delete("/api/review/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }
}