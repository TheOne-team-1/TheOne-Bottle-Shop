package one.theone.server.domain.search.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductRepository productRepository;
    private final SearchRankingService searchRankingService;
    private final SearchCacheService searchCacheService;

    @Transactional(readOnly = true)
    public PageResponse<ProductSearchResponse> searchByKeywordV1(String keyword, Pageable pageable) {
        PageResponse<ProductSearchResponse> page = productRepository.findProductByKeyword(keyword, pageable);

        if (!page.content().isEmpty()) {
            searchRankingService.record(keyword);
        }
        return page;
    }

    public PageResponse<ProductSearchResponse> searchByKeywordV2(String keyword, Pageable pageable) {
        PageResponse<ProductSearchResponse> page = searchCacheService.getOrCache(keyword, pageable);

        if (!page.content().isEmpty()) {
            searchRankingService.record(keyword);
        }
        return page;
    }
}
