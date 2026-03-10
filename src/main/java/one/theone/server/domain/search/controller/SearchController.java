package one.theone.server.domain.search.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import one.theone.server.domain.search.service.SearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search/v1")
    public ResponseEntity<BaseResponse<PageResponse<ProductSearchResponse>>> searchProductV1(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // TODO jwt를 통해 userId 가져오기
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "검색어에 해당하는 상품을 검색했습니다",
                searchService.searchByKeywordV1(keyword, pageable)));
    }
}
