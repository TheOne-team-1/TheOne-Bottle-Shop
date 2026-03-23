package one.theone.server.common.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCT_SEARCH = "productSearch";
    public static final String SEARCH_RANKING = "searchRanking";
    public static final String CART_CACHE = "cartCache";
    public static final String ORDER_LIST_CACHE = "orderListCache";
    public static final String ORDER_DETAIL_CACHE = "orderDetailCache";

    @Bean
    public CacheManager localCacheManager() {

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache(PRODUCT_SEARCH, 5, TimeUnit.MINUTES, 100),
                buildCache(SEARCH_RANKING, 10, TimeUnit.MINUTES, 1),
                buildCache(CART_CACHE, 10, TimeUnit.SECONDS, 10_000),
                buildCache(ORDER_LIST_CACHE, 30, TimeUnit.SECONDS, 10_000),
                buildCache(ORDER_DETAIL_CACHE, 1, TimeUnit.MINUTES, 10_000)
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name, long ttl, TimeUnit unit, long maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .expireAfterWrite(ttl, unit)
                .maximumSize(maxSize)
                .build());
    }
}
