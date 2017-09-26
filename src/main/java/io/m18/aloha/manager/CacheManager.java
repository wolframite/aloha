package io.m18.aloha.manager;

import io.m18.aloha.config.CacheConfig;

import org.infinispan.*;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class CacheManager {

    public static final String CACHE_NAME = "memcachedCache";

    private EmbeddedCacheManager embeddedCacheManager;
    private CacheConfig cacheConfig;

    @Autowired
    public CacheManager(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
        embeddedCacheManager = new DefaultCacheManager(cacheConfig.getGlobalConfiguration());

        // Configure primary cache
        embeddedCacheManager.defineConfiguration(
            cacheConfig.getCacheName(),
            cacheConfig.getCacheConfiguration()
        );
    }

    @Bean
    EmbeddedCacheManager embeddedCacheManager() {
        return embeddedCacheManager;
    }

    @Bean
    public AdvancedCache<String, byte[]> mainCache(EmbeddedCacheManager embeddedCacheManager) {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(CACHE_NAME);
        return cache.getAdvancedCache();
    }

}
