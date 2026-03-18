package one.theone.server.domain.product.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.*;
import one.theone.server.domain.product.service.ProductService;
import one.theone.server.domain.product.service.ProductViewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;
    private final ProductViewService productViewService;

    // 관리자 전용 -----------------------------------------------------------------------------------------
    @PostMapping("/admin/products")
    public ResponseEntity<BaseResponse<ProductCreateResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "상품 등록 성공", productService.createProduct(request)));
    }

    @GetMapping("/admin/products")
    public ResponseEntity<BaseResponse<PageResponse<AdminProductsGetResponse>>> getAdminProducts(
            @ModelAttribute AdminProductsGetRequest request,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "관리자 상품 목록 조회 성공", productService.getAdminProducts(request, pageable)));
    }


    @PatchMapping("/admin/products/{id}")
    public ResponseEntity<BaseResponse<ProductUpdateResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "상품 수정 성공", productService.updateProduct(id, request)));
    }

    @PatchMapping("/admin/products/{id}/status")
    public ResponseEntity<BaseResponse<ProductStatusUpdateResponse>> updateProductStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductStatusUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "상품 상태 변경 성공", productService.updateProductStatus(id, request)));
    }

    @DeleteMapping("/admin/products/{id}")
    public ResponseEntity<BaseResponse<ProductDeleteResponse>> deleteProduct(
            @PathVariable Long id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "상품 삭제 성공", productService.deleteProduct(id)));
    }


    // 일반 사용자 -----------------------------------------------------------------------------------------
    @GetMapping("/products")
    public ResponseEntity<BaseResponse<PageResponse<ProductsGetResponse>>> getProducts(
            @ModelAttribute ProductsGetRequest request,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "상품 목록 조회 성공", productService.getProducts(request, pageable)));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<BaseResponse<ProductGetResponse>> getProduct(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "상품 상세 조회 성공", productService.getProduct(id, clientIp)));
    }

    @GetMapping("/best/products")
    public ResponseEntity<BaseResponse<List<BestProductsGetResponse>>> getBestProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "베스트 상품 조회 성공", productViewService.getBestProducts()));
    }
}
