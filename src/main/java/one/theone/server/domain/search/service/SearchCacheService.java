package one.theone.server.domain.search.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static one.theone.server.common.config.cache.CacheConfig.PRODUCT_SEARCH;

@Service
@RequiredArgsConstructor
public class SearchCacheService {

    private final ProductRepository productRepository;

    @Cacheable(
            value = PRODUCT_SEARCH,
            key = "'keyword:' + #keyword + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize",
            cacheManager = "localCacheManager")
    @Transactional(readOnly = true)
    public PageResponse<ProductSearchResponse> getOrCache(List<String> keywordMorphemes, String keyword, Pageable pageable) {
        return productRepository.findProductByKeyword(keywordMorphemes, keyword, pageable);
    }
}
