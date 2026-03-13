package one.theone.server.domain.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.category.dto.*;
import one.theone.server.domain.category.service.CategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/categories")
    public ResponseEntity<BaseResponse<CategoryCreateResponse>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "대분류 카테고리 생성 성공", categoryService.createCategory(request)));
    }

    @PatchMapping("/categories/{id}")
    public ResponseEntity<BaseResponse<CategoryUpdateResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "대분류 카테고리 수정 성공", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<BaseResponse<CategoryDeleteResponse>> deleteCategory(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "대분류 카테고리 삭제 성공", categoryService.deleteCategory(id)));
    }

    @PostMapping("/category-details")
    public ResponseEntity<BaseResponse<CategoryDetailCreateResponse>> createCategoryDetail(
            @Valid @RequestBody CategoryDetailCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "소분류 카테고리 생성 성공", categoryService.createCategoryDetail(request)));
    }

    @PatchMapping("/category-details/{id}")
    public ResponseEntity<BaseResponse<CategoryDetailUpdateResponse>> updateCategoryDetail(
            @PathVariable Long id,
            @Valid @RequestBody CategoryDetailUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "소분류 카테고리 수정 성공", categoryService.updateCategoryDetail(id, request)));
    }

    @GetMapping("/categories")
    public ResponseEntity<BaseResponse<PageResponse<CategoriesGetResponse>>> getCategories(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "카테고리 목록 조회 성공", categoryService.getCategories(pageable)));
    }
}
