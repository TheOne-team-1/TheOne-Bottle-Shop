package one.theone.server.domain.search.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.search.dto.SearchResultResponse;
import one.theone.server.domain.search.service.SearchRankingService;
import one.theone.server.domain.search.service.SearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SearchRankingService searchRankingService;

    @GetMapping("/search/v1")
    public ResponseEntity<BaseResponse<SearchResultResponse>> searchProductV1(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "검색어에 해당하는 상품을 검색했습니다",
                searchService.searchByKeywordV1(keyword, pageable, userId, clientIp, userAgent)
        ));
    }

    @GetMapping("/search/v2")
    public ResponseEntity<BaseResponse<SearchResultResponse>> searchProductV2(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Long userId,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        String clientIp = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "검색어에 해당하는 상품을 검색했습니다",
                searchService.searchByKeywordV2(keyword, pageable, userId, clientIp, userAgent)
        ));
    }

    @GetMapping("/best/search")
    public ResponseEntity<BaseResponse<List<String>>> getBestKeyword() {
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.getReasonPhrase(),
                "인기 검색어를 조회했습니다",
                searchRankingService.getKeywordRanking()
        ));
    }

    private String extractClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
