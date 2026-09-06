package ru.ifmo.movies_app.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.ifmo.movies_app.service.AnalyticsCacheNames;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                AnalyticsCacheNames.GENRE_COUNTS,
                AnalyticsCacheNames.NAME_SEARCH,
                AnalyticsCacheNames.GENRE_LISTS,
                AnalyticsCacheNames.NO_OSCARS,
                AnalyticsCacheNames.SCREENWRITERS_NO_OSCARS);
    }
}
