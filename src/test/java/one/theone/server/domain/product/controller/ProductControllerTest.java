package one.theone.server.domain.product.controller;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.*;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.service.ProductService;
import one.theone.server.domain.product.service.ProductViewService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductViewService productViewService;

    @Test
    @DisplayName("상품 등록 성공")
    @WithMockUser
    void createProduct_success() throws Exception {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L
        );
        ProductCreateResponse response = new ProductCreateResponse(
                1L, "테스트 상품", 10000L, Product.ProductStatus.SALES,
                BigDecimal.valueOf(13.5), 750, 1L, 100L
        );
        given(productService.createProduct(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 등록 성공"))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"))
                .andExpect(jsonPath("$.data.status").value("SALES"));
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 성공")
    @WithMockUser
    void getAdminProducts_success() throws Exception {
        // given
        PageResponse<AdminProductsGetResponse> pageResponse = PageResponse.register(
                new PageImpl<>(List.of(
                        new AdminProductsGetResponse(
                                1L, "테스트 상품", 10000L,
                                AdminProductsGetResponse.AdminProductsStatus.SALES,
                                100L, BigDecimal.valueOf(4.5), LocalDateTime.now()
                        )
                ))
        );
        given(productService.getAdminProducts(any(), anyInt(), anyInt())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/admin/products")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("관리자 상품 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].status").value("SALES"));
    }

    @Test
    @DisplayName("상품 수정 성공")
    @WithMockUser
    void updateProduct_success() throws Exception {
        // given
        Long productId = 1L;
        ProductUpdateRequest request = new ProductUpdateRequest("수정된 상품", 20000L, null, null, null, null);
        ProductUpdateResponse response = new ProductUpdateResponse(
                1L, "수정된 상품", 20000L, BigDecimal.valueOf(13.5), 750, 1L, 100L
        );
        given(productService.updateProduct(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/admin/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("수정된 상품"));
    }

    @Test
    @DisplayName("상품 상태 변경 성공")
    @WithMockUser
    void updateProductStatus_success() throws Exception {
        // given
        Long productId = 1L;
        ProductStatusUpdateRequest request = new ProductStatusUpdateRequest(Product.ProductStatus.SOLD_OUT);
        ProductStatusUpdateResponse response = new ProductStatusUpdateResponse(1L, Product.ProductStatus.SOLD_OUT);
        given(productService.updateProductStatus(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/admin/products/{id}/status", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 상태 변경 성공"))
                .andExpect(jsonPath("$.data.status").value("SOLD_OUT"));
    }

    @Test
    @DisplayName("상품 삭제 성공")
    @WithMockUser
    void deleteProduct_success() throws Exception {
        // given
        Long productId = 1L;
        ProductDeleteResponse response = new ProductDeleteResponse(
                1L, "테스트 상품", true, LocalDateTime.now()
        );
        given(productService.deleteProduct(any())).willReturn(response);

        // when & then
        mockMvc.perform(delete("/api/admin/products/{id}", productId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 삭제 성공"))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    @DisplayName("상품 목록 조회 성공")
    @WithMockUser
    void getProducts_success() throws Exception {
        // given
        PageResponse<ProductsGetResponse> pageResponse = PageResponse.register(
                new PageImpl<>(List.of(
                        new ProductsGetResponse(1L, "테스트 상품", 10000L, Product.ProductStatus.SALES, BigDecimal.valueOf(4.5))
                ))
        );
        given(productService.getProducts(any(), anyInt(), anyInt())).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/products")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 목록 조회 성공"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    @WithMockUser
    void getProduct_success() throws Exception {
        // given
        Long productId = 1L;
        ProductGetResponse response = new ProductGetResponse(
                1L, "테스트 상품", 10000L, Product.ProductStatus.SALES,
                BigDecimal.valueOf(13.5), 750, 1L, 1L, 100L, BigDecimal.valueOf(4.5), 0L
        );
        given(productService.getProduct(any(), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/products/{id}", productId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 상세 조회 성공"))
                .andExpect(jsonPath("$.data.name").value("테스트 상품"));
    }

    @Test
    @DisplayName("베스트 상품 조회 성공")
    @WithMockUser
    void getBestProducts_success() throws Exception {
        // given
        given(productViewService.getBestProducts()).willReturn(
                List.of(new BestProductsGetResponse(1L, "베스트 상품", 50000L))
        );

        // when & then
        mockMvc.perform(get("/api/best/products")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("베스트 상품 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("베스트 상품"));
    }
}