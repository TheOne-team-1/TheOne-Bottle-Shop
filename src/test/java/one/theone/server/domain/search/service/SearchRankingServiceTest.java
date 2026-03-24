package one.theone.server.domain.search.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchRankingServiceTest {

    @InjectMocks
    private SearchRankingService searchRankingService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOps;
    @Mock
    private ZSetOperations<String, Object> zSetOps;

    @Nested
    @DisplayName("record()")
    class Record {

        @Test
        @DisplayName("로그인 유저가 최초 검색이면 ZSet 점수를 증가")
        void incrementScore_whenLoggedInUserFirstSearch() {
            // given
            String keyword = "맥주";
            Long userId = 1L;
            String clientIp = "127.0.0.1";
            String userAgent = "test-agent";

            when(valueOps.setIfAbsent(
                    contains("user:" + userId),
                    eq("locked"),
                    eq(60 * 30L),
                    eq(TimeUnit.SECONDS)
            )).thenReturn(true);
            when(redisTemplate.opsForValue()).thenReturn(valueOps);
            when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
            when(redisTemplate.getExpire(anyString())).thenReturn(-1L);

            // when
            searchRankingService.record(keyword, userId, clientIp, userAgent);

            // then
            verify(zSetOps).incrementScore(anyString(), eq(keyword), eq(1.0));
            verify(redisTemplate).expire(anyString(), eq(8L), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("비로그인 게스트가 최초 검색이면 ZSet 점수를 증가")
        void incrementScore_whenGuestFirstSearch() {
            // given
            String keyword = "와인";
            String clientIp = "192.168.0.1";
            String userAgent = "Mozilla";

            when(valueOps.setIfAbsent(
                    contains("guest:" + clientIp + ":" + userAgent),
                    eq("locked"),
                    eq(60 * 30L),
                    eq(TimeUnit.SECONDS)
            )).thenReturn(true);
            when(redisTemplate.opsForValue()).thenReturn(valueOps);
            when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
            when(redisTemplate.getExpire(anyString())).thenReturn(-1L);

            // when
            searchRankingService.record(keyword, null, clientIp, userAgent);

            // then
            verify(zSetOps).incrementScore(anyString(), eq(keyword), eq(1.0));
        }

        @Test
        @DisplayName("중복 검색(dedup 키 이미 존재)이면 ZSet 점수를 증가시키지 않음")
        void doNotIncrementScore_whenDuplicateSearch() {
            // given
            String keyword = "맥주";
            Long userId = 1L;

            when(redisTemplate.opsForValue()).thenReturn(valueOps);
            when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(false);

            // when
            searchRankingService.record(keyword, userId, "127.0.0.1", "agent");

            // then
            verify(zSetOps, never()).incrementScore(anyString(), anyString(), anyDouble());
        }

        @Test
        @DisplayName("랭킹 키에 이미 TTL이 설정되어 있으면 expire를 재설정하지 않음")
        void doNotResetExpire_whenTtlAlreadySet() {
            // given
            when(redisTemplate.opsForValue()).thenReturn(valueOps);
            when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
            when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
            when(redisTemplate.getExpire(anyString())).thenReturn(100L); // TTL 이미 있음

            // when
            searchRankingService.record("keyword", 1L, "ip", "agent");

            // then
            verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("getKeywordRanking()")
    class GetKeywordRanking {

        @Test
        @DisplayName("Redis ZSet에서 상위 5개 키워드를 내림차순으로 반환")
        void returnTop5Keywords() {
            // given
            Set<Object> redisResult = new LinkedHashSet<>(
                    List.of("와인", "막걸리", "소주", "맥주", "위스키")
            );
            when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
            when(zSetOps.reverseRange(anyString(), eq(0L), eq(4L))).thenReturn(redisResult);

            // when
            List<String> ranking = searchRankingService.getKeywordRanking();

            // then
            assertThat(ranking).hasSize(5);
            assertThat(ranking).containsExactly("와인", "막걸리", "소주", "맥주", "위스키");
        }

        @Test
        @DisplayName("Redis가 null을 반환하면 빈 리스트를 반환")
        void returnEmptyList_whenRedisReturnsNull() {
            // given
            when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
            when(zSetOps.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(null);

            // when
            List<String> ranking = searchRankingService.getKeywordRanking();

            // then
            assertThat(ranking).isEmpty();
        }

        @Test
        @DisplayName("ZSet 값이 String이 아닌 경우에도 toString()으로 변환하여 반환")
        void convertNonStringToString() {
            // given
            Set<Object> redisResult = new LinkedHashSet<>();
            redisResult.add(12345); // Integer가 들어온 경우
            when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
            when(zSetOps.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(redisResult);

            // when
            List<String> ranking = searchRankingService.getKeywordRanking();

            // then
            assertThat(ranking).containsExactly("12345");
        }
    }
}