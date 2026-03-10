package one.theone.server.domain.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.ProductCreateRequest;
import one.theone.server.domain.product.dto.ProductCreateResponse;
import one.theone.server.domain.product.dto.ProductsGetRequest;
import one.theone.server.domain.product.dto.ProductsGetResponse;
import one.theone.server.domain.product.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<BaseResponse<ProductCreateResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "상품 등록 성공", productService.createProduct(request)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<ProductsGetResponse>>> getProducts(
            @ModelAttribute ProductsGetRequest request,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.success(HttpStatus.OK.name(), "상품 목록 조회 성공", productService.getProducts(request, pageable)));
    }
}
