package one.theone.server.domain.favorite.controller;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.favorite.dto.FavoritesGetResponse;
import one.theone.server.domain.favorite.service.FavoriteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FavoriteService favoriteService;

    @Test
    @DisplayName("즐겨찾기 등록 성공")
    @WithMockUser
    void registerFavorite_success() throws Exception {
        // given
        Long productId = 10L;

        // when & then
        mockMvc.perform(post("/api/favorites/{productId}", productId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("즐겨찾기 등록 성공"));
    }

    @Test
    @DisplayName("즐겨찾기 삭제 성공")
    @WithMockUser
    void deleteFavorite_success() throws Exception {
        // given
        Long productId = 10L;

        // when & then
        mockMvc.perform(delete("/api/favorites/{productId}", productId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("즐겨찾기 삭제 성공"));

        verify(favoriteService).deleteFavorite(any(), eq(productId));
    }

    @Test
    @DisplayName("즐겨찾기 목록 조회 성공")
    @WithMockUser
    void getFavorites_success() throws Exception {
        // given
        PageResponse<FavoritesGetResponse> pageResponse = PageResponse.register(
                new PageImpl<>(List.of(
                        new FavoritesGetResponse(10L, FavoritesGetResponse.FavoriteProductStatus.SALES, LocalDateTime.now())
                ))
        );
        given(favoriteService.getFavorites(any(), eq(null), anyInt(), anyInt())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/favorites")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("즐겨찾기 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("SALES"));
    }
}