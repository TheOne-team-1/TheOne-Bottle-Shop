package one.theone.server.domain.search.service;

import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.product.dto.BestProductsGetResponse;
import one.theone.server.domain.product.repository.ProductRepository;
import one.theone.server.domain.product.service.ProductViewService;
import one.theone.server.domain.search.corrector.EngToKorCorrector;
import one.theone.server.domain.search.corrector.KomoranCorrector;
import one.theone.server.domain.search.dto.ProductSearchResponse;
import one.theone.server.domain.search.dto.SearchResultResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @InjectMocks
    private SearchService searchService;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private SearchRankingService searchRankingService;
    @Mock
    private SearchCacheService searchCacheService;
    @Mock
    private KomoranCorrector komoranCorrector;
    @Mock
    private EngToKorCorrector engToKorCorrector;
    @Mock
    private ProductViewService productViewService;

    private final Pageable pageable = PageRequest.of(0, 10);
    private final Long userId = 1L;
    private final String clientIp = "127.0.0.1";
    private final String userAgent = "test-agent";

    @Nested
    @DisplayName("searchByKeywordV1")
    class SearchByKeywordV1 {
        @Test
        @DisplayName("검색 결과 존재 시 검색 결과 반환과 검색어 랭킹 기록")
        void returnResultAndRecordRanking_whenResultExists() {
            //given
            String keyword = "  와인  ";
            String normalized = "와인";
            List<String> morphemes = List.of("와인");
            PageResponse<ProductSearchResponse> page = pageOf(
                    List.of(new ProductSearchResponse("와인 프리미엄 맥스 크림", 50000L, "프리미엄", "와인")));
            when(komoranCorrector.extractMorphemes(normalized)).thenReturn(morphemes);
            when(productRepository.findProductByKeyword(morphemes, normalized, pageable)).thenReturn(page);

            //when
            SearchResultResponse result = searchService.searchByKeywordV1(keyword, pageable, userId, clientIp, userAgent);

            //then
            assertThat(result.page()).isEqualTo(page);
            assertThat(result.suggestion()).isNull();
            assertThat(result.recommendedProduct()).isNull();
            verify(searchRankingService).record(normalized, userId, clientIp, userAgent);
        }

        @Test
        @DisplayName("검색 결과가 없고 영타 교정이 가능하면 교정어를 제안")
        void returnSuggestion_whenNoResultAndEngTypoExists() {
            // given
            String keyword = "dhkdls";
            String normalized = "dhkdls";
            List<String> morphemes = List.of("dhkdls");
            PageResponse<ProductSearchResponse> emptyPage = pageOf(List.of());

            when(komoranCorrector.extractMorphemes(normalized)).thenReturn(morphemes);
            when(productRepository.findProductByKeyword(morphemes, normalized, pageable)).thenReturn(emptyPage);
            when(engToKorCorrector.correct(normalized)).thenReturn(Optional.of("와인"));

            // when
            SearchResultResponse result = searchService.searchByKeywordV1(keyword, pageable, userId, clientIp, userAgent);

            // then
            assertThat(result.page()).isEqualTo(emptyPage);
            assertThat(result.suggestion()).isEqualTo("혹시 '와인'을(를) 찾으셨나요?");
            assertThat(result.recommendedProduct()).isNull();
            verify(searchRankingService, never()).record(any(), any(), any(), any());
        }

        @Test
        @DisplayName("검색 결과도 없고 영타 교정도 불가능하면 베스트 상품을 추천")
        void returnBestProduct_whenNoResultAndNoCorrection() {
            // given
            String keyword = "ㅋㅋ";
            String normalized = "ㅋㅋ";
            List<String> morphemes = List.of("ㅋㅋ");
            PageResponse<ProductSearchResponse> emptyPage = pageOf(List.of());
            BestProductsGetResponse bestProduct = mock(BestProductsGetResponse.class);

            when(komoranCorrector.extractMorphemes(normalized)).thenReturn(morphemes);
            when(productRepository.findProductByKeyword(morphemes, normalized, pageable)).thenReturn(emptyPage);
            when(engToKorCorrector.correct(normalized)).thenReturn(Optional.empty());
            when(productViewService.getBestProducts()).thenReturn(List.of(bestProduct));

            // when
            SearchResultResponse result = searchService.searchByKeywordV1(keyword, pageable, userId, clientIp, userAgent);

            // then
            assertThat(result.suggestion()).isEqualTo("베스트 상품은 어떠신가요?");
            assertThat(result.recommendedProduct()).isEqualTo(bestProduct);
            verify(searchRankingService, never()).record(any(), any(), any(), any());
        }

        @Test
        @DisplayName("검색 결과도 없고 영타 교정도 불가능하고 베스트 상품도 없으면 recommendedProduct는 null")
        void returnNullRecommendedProduct_whenBestProductsEmpty() {
            // given
            String keyword = "ㅋㅋ";
            String normalized = "ㅋㅋ";
            List<String> morphemes = List.of("ㅋㅋ");
            PageResponse<ProductSearchResponse> emptyPage = pageOf(List.of());

            when(komoranCorrector.extractMorphemes(normalized)).thenReturn(morphemes);
            when(productRepository.findProductByKeyword(morphemes, normalized, pageable)).thenReturn(emptyPage);
            when(engToKorCorrector.correct(normalized)).thenReturn(Optional.empty());
            when(productViewService.getBestProducts()).thenReturn(List.of());

            // when
            SearchResultResponse result = searchService.searchByKeywordV1(keyword, pageable, userId, clientIp, userAgent);

            // then
            assertThat(result.suggestion()).isEqualTo("베스트 상품은 어떠신가요?");
            assertThat(result.recommendedProduct()).isNull();
        }

        @Test
        @DisplayName("키워드 앞뒤 공백이 제거되고 소문자로 정규화")
        void normalizeKeyword_trimAndLowerCase() {
            // given
            String keyword = "  WINE  ";
            String normalized = "wine";
            List<String> morphemes = List.of("wine");
            PageResponse<ProductSearchResponse> page = pageOf(
                    List.of(new ProductSearchResponse("wine premium max cream", 100000L, "프리미엄", "와인"))
            );

            when(komoranCorrector.extractMorphemes(normalized)).thenReturn(morphemes);
            when(productRepository.findProductByKeyword(morphemes, normalized, pageable)).thenReturn(page);

            // when
            searchService.searchByKeywordV1(keyword, pageable, userId, clientIp, userAgent);

            // then // 정규화된 키워드로 호출 확인
            verify(komoranCorrector).extractMorphemes(normalized);
            verify(productRepository).findProductByKeyword(morphemes, normalized, pageable);
            verify(searchRankingService).record(normalized, userId, clientIp, userAgent);
        }
    }

    @Nested
    @DisplayName("searchByKeywordV2")
    class SearchByKeywordV2 {
        @Test
        @DisplayName("V2는 Repository 대신 CacheService를 경유") // 캐시 히트, 미스 테스트는 통합테스트를 통해 진행
        void useCache_insteadOfRepository() {
            // given
            String keyword = "막걸리";
            String normalized = "막걸리";
            List<String> morphemes = List.of("막걸리");
            PageResponse<ProductSearchResponse> page = pageOf(
                    List.of(new ProductSearchResponse("막걸리 프리미엄 카스 후레쉬", 80000L, "프리미엄", "막걸리"))
            );

            when(komoranCorrector.extractMorphemes(normalized)).thenReturn(morphemes);
            when(searchCacheService.getOrCache(morphemes, normalized, pageable)).thenReturn(page);

            // when
            searchService.searchByKeywordV2(keyword, pageable, userId, clientIp, userAgent);

            // then
            verify(searchCacheService).getOrCache(morphemes, normalized, pageable);
            verify(productRepository, never()).findProductByKeyword(any(), any(), any());
        }
    }

    private <T> PageResponse<T> pageOf(List<T> content) {
        return new PageResponse<>(
                content,
                0,
                1,
                content.size(),
                10,
                true
        );
    }
}