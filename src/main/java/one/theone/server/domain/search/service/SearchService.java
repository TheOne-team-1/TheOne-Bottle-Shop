package one.theone.server.domain.search.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.repository.ProductRepository;
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

    @Transactional(readOnly = true)
    public SearchResultResponse searchByKeywordV1(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        List<String> keywordMorphemes = komoranCorrector.extractMorphemes(normalizedKeyword);
        return searchResult(productRepository.findProductByKeyword(keywordMorphemes, normalizedKeyword, pageable), normalizedKeyword);
    }

    public SearchResultResponse searchByKeywordV2(String keyword, Pageable pageable) {
        String normalizedKeyword = keyword.trim().toLowerCase();
        List<String> keywordMorphemes = komoranCorrector.extractMorphemes(normalizedKeyword);
        return searchResult(searchCacheService.getOrCache(keywordMorphemes, normalizedKeyword, pageable), normalizedKeyword);

    }

    private SearchResultResponse searchResult(PageResponse<ProductSearchResponse> page, String keyword) {
        if (!page.content().isEmpty()) {
            searchRankingService.record(keyword);
            return new SearchResultResponse(page, null);
        }

        // TODO 베스트 상품 기능 추가 시 제안 수정
        return engToKorCorrector.correct(keyword)
                .map(s -> new SearchResultResponse(page, "혹시 '" + s + "'을(를) 찾으셨나요?"))
                .orElseGet(() -> new SearchResultResponse(page, "베스트 상품은 어떠신가요?"));
    }
}
