package one.theone.server.domain.product.service;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.category.entity.CategoryDetail;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.product.dto.*;
import one.theone.server.domain.product.entity.Product;
import one.theone.server.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryDetailRepository categoryDetailRepository;
    @Mock private ProductViewService productViewService;

    @InjectMocks private ProductService productService;

    @Test
    @DisplayName("상품 등록 성공")
    void createProduct_success() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L
        );
        CategoryDetail categoryDetail = CategoryDetail.register(1L, "위스키", 1);
        given(categoryDetailRepository.findById(1L)).willReturn(Optional.of(categoryDetail));

        // when
        ProductCreateResponse response = productService.createProduct(request);

        // then
        assertThat(response.name()).isEqualTo("테스트 상품");
        assertThat(response.price()).isEqualTo(10000L);
        assertThat(response.status()).isEqualTo(Product.ProductStatus.SALES);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 등록 실패 - 카테고리 없음")
    void createProduct_categoryNotFound() {
        // given
        ProductCreateRequest request = new ProductCreateRequest(
                "테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 999L, 100L
        );
        given(categoryDetailRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("관리자 상품 목록 조회 성공")
    void getAdminProducts_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        AdminProductsGetRequest request = new AdminProductsGetRequest(
                AdminProductsGetRequest.ProductSortType.LATEST, null, null, null, null, null, null, null
        );
        AdminProductsGetResponse item = new AdminProductsGetResponse(
                1L, "테스트 상품", 10000L,
                AdminProductsGetResponse.AdminProductsStatus.SALES,
                100L, BigDecimal.valueOf(4.5), LocalDateTime.now()
        );
        given(productRepository.findAdminProductWithConditions(pageable, request))
                .willReturn(new PageImpl<>(List.of(item)));

        // when
        PageResponse<AdminProductsGetResponse> result = productService.getAdminProducts(request, pageable);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status())
                .isEqualTo(AdminProductsGetResponse.AdminProductsStatus.SALES);
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct_success() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        ProductUpdateRequest request = new ProductUpdateRequest("수정된 상품", 20000L, null, null, 2L, null);
        CategoryDetail newCategory = CategoryDetail.register(1L, "브랜디", 2);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(categoryDetailRepository.findById(2L)).willReturn(Optional.of(newCategory));

        // when
        ProductUpdateResponse response = productService.updateProduct(productId, request);

        // then
        assertThat(response.name()).isEqualTo("수정된 상품");
        assertThat(response.price()).isEqualTo(20000L);
        assertThat(response.productCategoryDetailId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("상품 수정 실패 - 상품 없음")
    void updateProduct_productNotFound() {
        // given
        given(productRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(
                999L, new ProductUpdateRequest(null, null, null, null, null, null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 수정 실패 - 삭제된 상품")
    void updateProduct_deletedProduct() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        product.delete();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(
                productId, new ProductUpdateRequest("수정", null, null, null, null, null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("삭제된 상품은 수정할 수 없습니다");
    }

    @Test
    @DisplayName("상품 수정 실패 - 변경할 카테고리 없음")
    void updateProduct_categoryNotFound() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        ProductUpdateRequest request = new ProductUpdateRequest(null, null, null, null, 999L, null);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));
        given(categoryDetailRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(productId, request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 수정 실패 - 단종 상품")
    void updateProduct_discontinuedProduct() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        product.updateStatus(new ProductStatusUpdateRequest(Product.ProductStatus.DISCONTINUE));
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateProduct(
                productId, new ProductUpdateRequest("수정", null, null, null, null, null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("단종된 상품은 수정할 수 없습니다");
    }

    @Test
    @DisplayName("상품 상태 변경 성공")
    void updateProductStatus_success() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        ProductStatusUpdateRequest request = new ProductStatusUpdateRequest(Product.ProductStatus.SOLD_OUT);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductStatusUpdateResponse response = productService.updateProductStatus(productId, request);

        // then
        assertThat(response.status()).isEqualTo(Product.ProductStatus.SOLD_OUT);
    }

    @Test
    @DisplayName("상품 상태 변경 실패 - 상품 없음")
    void updateProductStatus_productNotFound() {
        // given
        given(productRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.updateProductStatus(
                999L, new ProductStatusUpdateRequest(Product.ProductStatus.SOLD_OUT)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 상태 변경 실패 - 삭제된 상품")
    void updateProductStatus_deletedProduct() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        product.delete();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateProductStatus(
                productId, new ProductStatusUpdateRequest(Product.ProductStatus.SOLD_OUT)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("삭제된 상품은 수정할 수 없습니다");
    }

    @Test
    @DisplayName("상품 상태 변경 실패 - 단종 상품")
    void updateProductStatus_discontinuedProduct() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        product.updateStatus(new ProductStatusUpdateRequest(Product.ProductStatus.DISCONTINUE));
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateProductStatus(
                productId, new ProductStatusUpdateRequest(Product.ProductStatus.SOLD_OUT)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("단종된 상품은 수정할 수 없습니다");
    }

    @Test
    @DisplayName("상품 상태 변경 실패 - 재고 없는 상품을 판매중으로 변경")
    void updateProductStatus_outOfStockToSales() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 0L); // quantity=0 → SOLD_OUT
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.updateProductStatus(
                productId, new ProductStatusUpdateRequest(Product.ProductStatus.SALES)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("재고가 없는 상품은 판매 중으로 변경할 수 없습니다");
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProduct_success() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        ProductDeleteResponse response = productService.deleteProduct(productId);

        // then
        assertThat(response.deleted()).isTrue();
        assertThat(response.deletedAt()).isNotNull();
    }

    @Test
    @DisplayName("상품 삭제 실패 - 상품 없음")
    void deleteProduct_productNotFound() {
        // given
        given(productRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 삭제 실패 - 이미 삭제된 상품")
    void deleteProduct_alreadyDeleted() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        product.delete();
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("이미 삭제된 상품입니다");
    }

    @Test
    @DisplayName("상품 목록 조회 성공")
    void getProducts_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ProductsGetRequest request = new ProductsGetRequest(null, null, null, null, null, null, null);
        ProductsGetResponse item = new ProductsGetResponse(
                1L, "테스트 상품", 10000L, Product.ProductStatus.SALES, BigDecimal.valueOf(4.5)
        );
        given(productRepository.findProductWithConditions(pageable, request))
                .willReturn(new PageImpl<>(List.of(item)));

        // when
        PageResponse<ProductsGetResponse> result = productService.getProducts(request, pageable);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).status()).isEqualTo(Product.ProductStatus.SALES);
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProduct_success() {
        // given
        Long productId = 1L;
        ProductGetResponse response = new ProductGetResponse(
                1L, "테스트 상품", 10000L, Product.ProductStatus.SALES,
                BigDecimal.valueOf(13.5), 750, 1L, 1L, 100L, BigDecimal.valueOf(4.5), 0L
        );
        given(productRepository.findProductById(productId)).willReturn(response);
        given(productViewService.getViewCount(productId)).willReturn(10L);

        // when
        ProductGetResponse result = productService.getProduct(productId, "127.0.0.1");

        // then
        assertThat(result.viewCount()).isEqualTo(10L);
        verify(productViewService).record(productId, "127.0.0.1");
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 상품 없음")
    void getProduct_notFound() {
        // given
        given(productRepository.findProductById(any())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> productService.getProduct(999L, "127.0.0.1"))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("재고 차감 성공")
    void decreaseStock_success() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        boolean result = productService.decreaseStock(productId, 50L);

        // then
        assertThat(result).isFalse();
        assertThat(product.getQuantity()).isEqualTo(50L);
    }

    @Test
    @DisplayName("재고 차감 실패 - 재고 부족")
    void decreaseStock_insufficientStock() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 5L);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when & then
        assertThatThrownBy(() -> productService.decreaseStock(productId, 10L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("상품 재고가 부족합니다");
    }

    @Test
    @DisplayName("재고 증가 성공")
    void increaseStock_success() {
        // given
        Long productId = 1L;
        Product product = Product.register("테스트 상품", 10000L, BigDecimal.valueOf(13.5), 750, 1L, 100L);
        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        // when
        productService.increaseStock(productId, 50L);

        // then
        assertThat(product.getQuantity()).isEqualTo(150L);
    }
}