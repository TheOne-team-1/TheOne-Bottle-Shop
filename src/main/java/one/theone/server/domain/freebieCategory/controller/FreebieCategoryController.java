package one.theone.server.domain.freebieCategory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryCreateRequest;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryDetailCreateRequest;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryDetailUpdateRequest;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryUpdateRequest;
import one.theone.server.domain.freebieCategory.dto.response.*;
import one.theone.server.domain.freebieCategory.service.FreebieCategoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class FreebieCategoryController {
    private final FreebieCategoryService freebieCategoryService;

    @PostMapping("/freebie-categories")
    public ResponseEntity<BaseResponse<FreebieCategoryCreateResponse>> createFreebieCategory(
            @Valid @RequestBody FreebieCategoryCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "사은품 카테고리 생성 성공", freebieCategoryService.createFreebieCategory(request)));
    }

    @PatchMapping("/freebie-categories/{id}")
    public ResponseEntity<BaseResponse<FreebieCategoryUpdateResponse>> updateFreebieCategory(
            @PathVariable Long id,
            @Valid @RequestBody FreebieCategoryUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 카테고리 수정 성공", freebieCategoryService.updateFreebieCategory(id, request)));
    }

    @DeleteMapping("/freebie-categories/{id}")
    public ResponseEntity<BaseResponse<FreebieCategoryDeleteResponse>> deleteFreebieCategory(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 카테고리 삭제 성공", freebieCategoryService.deleteFreebieCategory(id)));
    }

    @PostMapping("/freebie-category-details")
    public ResponseEntity<BaseResponse<FreebieCategoryDetailCreateResponse>> createFreebieCategoryDetail(
            @Valid @RequestBody FreebieCategoryDetailCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "사은품 세부 카테고리 생성 성공", freebieCategoryService.createFreebieCategoryDetail(request)));
    }

    @PatchMapping("/freebie-category-details/{id}")
    public ResponseEntity<BaseResponse<FreebieCategoryDetailUpdateResponse>> updateFreebieCategoryDetail(
            @PathVariable Long id,
            @Valid @RequestBody FreebieCategoryDetailUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 세부 카테고리 수정 성공", freebieCategoryService.updateFreebieCategoryDetail(id, request)));
    }

    @DeleteMapping("/freebie-category-details/{id}")
    public ResponseEntity<BaseResponse<FreebieCategoryDetailDeleteResponse>> deleteFreebieCategoryDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 세부 카테고리 삭제 성공", freebieCategoryService.deleteFreebieCategoryDetail(id)));
    }

    @GetMapping("/freebie-categories")
    public ResponseEntity<BaseResponse<PageResponse<FreebieCategoriesGetResponse>>> getFreebieCategories(
            @RequestParam(defaultValue = "0") int page
            , @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "사은품 카테고리 목록 조회 성공", freebieCategoryService.getFreebieCategories(PageRequest.of(page, size))));
    }
}
