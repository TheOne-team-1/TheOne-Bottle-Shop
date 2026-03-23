package one.theone.server.domain.category.controller;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.category.dto.*;
import one.theone.server.domain.category.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CategoryService categoryService;

    @Test
    @DisplayName("대분류 카테고리 생성 성공")
    @WithMockUser
    void createCategory_success() throws Exception {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("위스키", 1);
        CategoryCreateResponse response = new CategoryCreateResponse(1L, "위스키", 1);
        given(categoryService.createCategory(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("대분류 카테고리 생성 성공"))
                .andExpect(jsonPath("$.data.name").value("위스키"));
    }

    @Test
    @DisplayName("대분류 카테고리 수정 성공")
    @WithMockUser
    void updateCategory_success() throws Exception {
        // given
        CategoryUpdateRequest request = new CategoryUpdateRequest("브랜디", null);
        CategoryUpdateResponse response = new CategoryUpdateResponse(1L, "브랜디", 1);
        given(categoryService.updateCategory(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/admin/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("대분류 카테고리 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("브랜디"));
    }

    @Test
    @DisplayName("대분류 카테고리 삭제 성공")
    @WithMockUser
    void deleteCategory_success() throws Exception {
        // given
        CategoryDeleteResponse response = new CategoryDeleteResponse(1L, "위스키", true, null);
        given(categoryService.deleteCategory(any())).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/admin/categories/{id}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("대분류 카테고리 삭제 성공"))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    @DisplayName("소분류 카테고리 생성 성공")
    @WithMockUser
    void createCategoryDetail_success() throws Exception {
        // given
        CategoryDetailCreateRequest request = new CategoryDetailCreateRequest(1L, "싱글몰트", 1);
        CategoryDetailCreateResponse response = new CategoryDetailCreateResponse(1L, 1L, "싱글몰트", 1);
        given(categoryService.createCategoryDetail(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/admin/category-details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("소분류 카테고리 생성 성공"))
                .andExpect(jsonPath("$.data.name").value("싱글몰트"));
    }

    @Test
    @DisplayName("소분류 카테고리 수정 성공")
    @WithMockUser
    void updateCategoryDetail_success() throws Exception {
        // given
        CategoryDetailUpdateRequest request = new CategoryDetailUpdateRequest(null, "블렌디드", null);
        CategoryDetailUpdateResponse response = new CategoryDetailUpdateResponse(1L, "블렌디드", 1);
        given(categoryService.updateCategoryDetail(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/admin/category-details/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("소분류 카테고리 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("블렌디드"));
    }

    @Test
    @DisplayName("소분류 카테고리 삭제 성공")
    @WithMockUser
    void deleteCategoryDetail_success() throws Exception {
        // given
        CategoryDetailDeleteResponse response = new CategoryDetailDeleteResponse(1L, "싱글몰트", true, null);
        given(categoryService.deleteCategoryDetail(any())).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/admin/category-details/{id}", 1L)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("소분류 카테고리 삭제 성공"))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    @WithMockUser
    void getCategories_success() throws Exception {
        // given
        PageResponse<CategoriesGetResponse> pageResponse = PageResponse.register(
                new PageImpl<>(List.of(new CategoriesGetResponse(1L, "위스키", 1, List.of())))
        );
        given(categoryService.getCategories(anyInt(), anyInt())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/admin/categories")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("위스키"));
    }
}