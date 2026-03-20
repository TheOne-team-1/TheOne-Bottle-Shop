package one.theone.server.domain.category.service;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.domain.category.dto.*;
import one.theone.server.domain.category.entity.Category;
import one.theone.server.domain.category.entity.CategoryDetail;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.category.repository.CategoryRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryDetailRepository categoryDetailRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private CategoryService categoryService;

    @Test
    @DisplayName("대분류 카테고리 생성 성공")
    void createCategory_success() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("위스키", 1);
        given(categoryRepository.existsByName("위스키")).willReturn(false);
        given(categoryRepository.existsBySortNum(1)).willReturn(false);

        // when
        CategoryCreateResponse response = categoryService.createCategory(request);

        // then
        assertThat(response.name()).isEqualTo("위스키");
        assertThat(response.sortNum()).isEqualTo(1);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("대분류 카테고리 생성 실패 - 중복 이름")
    void createCategory_duplicateName() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest("위스키", 1);
        given(categoryRepository.existsByName("위스키")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("이미 존재하는 대분류 카테고리입니다");
    }

    @Test
    @DisplayName("대분류 카테고리 수정 성공")
    void updateCategory_success() {
        // given
        Long categoryId = 1L;
        Category category = Category.register("위스키", 1);
        CategoryUpdateRequest request = new CategoryUpdateRequest("브랜디", 2);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryRepository.existsByName("브랜디")).willReturn(false);
        given(categoryRepository.existsBySortNum(2)).willReturn(false);

        // when
        CategoryUpdateResponse response = categoryService.updateCategory(categoryId, request);

        // then
        assertThat(response.name()).isEqualTo("브랜디");
        assertThat(response.sortNum()).isEqualTo(2);
    }

    @Test
    @DisplayName("대분류 카테고리 수정 실패 - 카테고리 없음")
    void updateCategory_notFound() {
        // given
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(999L, new CategoryUpdateRequest("브랜디", null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("대분류 카테고리 수정 실패 - 중복 이름")
    void updateCategory_duplicateName() {
        // given
        Long categoryId = 1L;
        Category category = Category.register("위스키", 1);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryRepository.existsByName("브랜디")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, new CategoryUpdateRequest("브랜디", null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("이미 존재하는 대분류 카테고리입니다");
    }

    @Test
    @DisplayName("대분류 카테고리 삭제 성공")
    void deleteCategory_success() {
        // given
        Long categoryId = 1L;
        Category category = Category.register("위스키", 1);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryDetailRepository.existsByCategoryIdAndDeletedFalse(categoryId)).willReturn(false);

        // when
        CategoryDeleteResponse response = categoryService.deleteCategory(categoryId);

        // then
        assertThat(response.deleted()).isTrue();
        assertThat(response.deletedAt()).isNotNull();
    }

    @Test
    @DisplayName("대분류 카테고리 삭제 실패 - 카테고리 없음")
    void deleteCategory_notFound() {
        // given
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("대분류 카테고리 삭제 실패 - 소분류 존재")
    void deleteCategory_hasDetails() {
        // given
        Long categoryId = 1L;
        Category category = Category.register("위스키", 1);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryDetailRepository.existsByCategoryIdAndDeletedFalse(categoryId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("소분류 카테고리가 존재하여 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("소분류 카테고리 생성 성공")
    void createCategoryDetail_success() {
        // given
        CategoryDetailCreateRequest request = new CategoryDetailCreateRequest(1L, "싱글몰트", 1);
        Category category = Category.register("위스키", 1);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryDetailRepository.existsByCategoryIdAndName(1L, "싱글몰트")).willReturn(false);
        given(categoryDetailRepository.existsByCategoryIdAndSortNum(1L, 1)).willReturn(false);

        // when
        CategoryDetailCreateResponse response = categoryService.createCategoryDetail(request);

        // then
        assertThat(response.name()).isEqualTo("싱글몰트");
        assertThat(response.categoryId()).isEqualTo(1L);
        verify(categoryDetailRepository).save(any(CategoryDetail.class));
    }

    @Test
    @DisplayName("소분류 카테고리 생성 실패 - 대분류 없음")
    void createCategoryDetail_categoryNotFound() {
        // given
        CategoryDetailCreateRequest request = new CategoryDetailCreateRequest(999L, "싱글몰트", 1);
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.createCategoryDetail(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("소분류 카테고리 생성 실패 - 중복 이름")
    void createCategoryDetail_duplicateName() {
        // given
        CategoryDetailCreateRequest request = new CategoryDetailCreateRequest(1L, "싱글몰트", 1);
        Category category = Category.register("위스키", 1);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(categoryDetailRepository.existsByCategoryIdAndName(1L, "싱글몰트")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategoryDetail(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리에 이미 존재하는 소분류 카테고리입니다");
    }

    @Test
    @DisplayName("소분류 카테고리 수정 성공")
    void updateCategoryDetail_success() {
        // given
        Long detailId = 1L;
        CategoryDetail detail = CategoryDetail.register(1L, "싱글몰트", 1);
        CategoryDetailUpdateRequest request = new CategoryDetailUpdateRequest(null, "블렌디드", null);
        given(categoryDetailRepository.findById(detailId)).willReturn(Optional.of(detail));
        given(categoryDetailRepository.existsByCategoryIdAndName(1L, "블렌디드")).willReturn(false);

        // when
        CategoryDetailUpdateResponse response = categoryService.updateCategoryDetail(detailId, request);

        // then
        assertThat(response.name()).isEqualTo("블렌디드");
    }

    @Test
    @DisplayName("소분류 카테고리 수정 실패 - 소분류 없음")
    void updateCategoryDetail_notFound() {
        // given
        given(categoryDetailRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategoryDetail(999L, new CategoryDetailUpdateRequest(null, "블렌디드", null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("소분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("소분류 카테고리 수정 실패 - 변경할 대분류 없음")
    void updateCategoryDetail_categoryNotFound() {
        // given
        Long detailId = 1L;
        CategoryDetail detail = CategoryDetail.register(1L, "싱글몰트", 1);
        given(categoryDetailRepository.findById(detailId)).willReturn(Optional.of(detail));
        given(categoryRepository.existsById(999L)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategoryDetail(detailId, new CategoryDetailUpdateRequest(999L, null, null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("소분류 카테고리 수정 실패 - 중복 이름")
    void updateCategoryDetail_duplicateName() {
        // given
        Long detailId = 1L;
        CategoryDetail detail = CategoryDetail.register(1L, "싱글몰트", 1);
        given(categoryDetailRepository.findById(detailId)).willReturn(Optional.of(detail));
        given(categoryDetailRepository.existsByCategoryIdAndName(1L, "블렌디드")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.updateCategoryDetail(detailId, new CategoryDetailUpdateRequest(null, "블렌디드", null)))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("대분류 카테고리에 이미 존재하는 소분류 카테고리입니다");
    }

    @Test
    @DisplayName("소분류 카테고리 삭제 성공")
    void deleteCategoryDetail_success() {
        // given
        Long detailId = 1L;
        CategoryDetail detail = CategoryDetail.register(1L, "싱글몰트", 1);
        given(categoryDetailRepository.findById(detailId)).willReturn(Optional.of(detail));
        given(productRepository.existsByCategoryDetailIdAndDeletedFalse(detailId)).willReturn(false);

        // when
        CategoryDetailDeleteResponse response = categoryService.deleteCategoryDetail(detailId);

        // then
        assertThat(response.deleted()).isTrue();
        assertThat(response.deletedAt()).isNotNull();
    }

    @Test
    @DisplayName("소분류 카테고리 삭제 실패 - 소분류 없음")
    void deleteCategoryDetail_notFound() {
        // given
        given(categoryDetailRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategoryDetail(999L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("소분류 카테고리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("소분류 카테고리 삭제 실패 - 상품 존재")
    void deleteCategoryDetail_hasProduct() {
        // given
        Long detailId = 1L;
        CategoryDetail detail = CategoryDetail.register(1L, "싱글몰트", 1);
        given(categoryDetailRepository.findById(detailId)).willReturn(Optional.of(detail));
        given(productRepository.existsByCategoryDetailIdAndDeletedFalse(detailId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategoryDetail(detailId))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("상품이 존재하여 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategories_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        CategoriesGetResponse item = new CategoriesGetResponse(1L, "위스키", 1, List.of());
        given(categoryRepository.findAllCategories(pageable)).willReturn(new PageImpl<>(List.of(item)));

        // when
        PageResponse<CategoriesGetResponse> result = categoryService.getCategories(0, 10);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).name()).isEqualTo("위스키");
    }
}