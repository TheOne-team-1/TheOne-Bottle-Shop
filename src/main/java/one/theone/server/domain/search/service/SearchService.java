package one.theone.server.domain.search.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.BestProductsGetResponse;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.product.service.ProductViewService;
import one.theone.server.domain.search.corrector.EngToKorCorrector;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import one.theone.server.domain.search.dto.SearchResultResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductRepository productRepository;
    private final SearchRankingService searchRankingService;
    private final SearchCacheService searchCacheService;
    private final KomoranCorrector komoranCorrector;
    private final EngToKorCorrector engToKorCorrector;
    private final ProductViewService productViewService;

    @Transactional(readOnly = true)
    public SearchResultResponse searchByKeywordV1(String keyword, Pageable pageable, Long userId, String clientIp, String userAgent) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        List<String> keywordMorphemes = komoranCorrector.extractMorphemes(normalizedKeyword);
        return searchResult(productRepository.findProductByKeyword(keywordMorphemes, normalizedKeyword, pageable), normalizedKeyword, userId, clientIp, userAgent);
    }

    public SearchResultResponse searchByKeywordV2(String keyword, Pageable pageable, Long userId, String clientIp, String userAgent) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        List<String> keywordMorphemes = komoranCorrector.extractMorphemes(normalizedKeyword);
        return searchResult(searchCacheService.getOrCache(keywordMorphemes, normalizedKeyword, pageable), normalizedKeyword, userId, clientIp, userAgent);

    }

    private SearchResultResponse searchResult(PageResponse<ProductSearchResponse> page, String keyword, Long userId, String clientIp, String userAgent) {
        if (!page.content().isEmpty()) {
            searchRankingService.record(keyword, userId, clientIp, userAgent);
            return new SearchResultResponse(page, null, null);
        }

        return engToKorCorrector.correct(keyword)
                .map(s -> new SearchResultResponse(page, "혹시 '" + s + "'을(를) 찾으셨나요?", null))
                .orElseGet(() -> {
                    List<BestProductsGetResponse> bestProducts = productViewService.getBestProducts();
                    BestProductsGetResponse recommendedProduct = bestProducts.isEmpty() ? null : bestProducts.getFirst();
                    return new SearchResultResponse(page, "베스트 상품은 어떠신가요?", recommendedProduct);
                });
    }
}
