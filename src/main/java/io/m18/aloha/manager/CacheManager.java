package io.m18.aloha.manager;

import io.m18.aloha.config.CacheConfig;

import io.m18.aloha.memcached.MemcachedItem;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class CacheManager {

    private EmbeddedCacheManager embeddedCacheManager;
    private CacheConfig cacheConfig;

    @Autowired
    public CacheManager(CacheConfig cacheConfig) {
        Assert.isTrue(cacheConfig.isPrimaryCacheEnabled() || cacheConfig.isSecondaryCacheEnabled(),
            "At least one Cache must be enabled"
        );

        this.cacheConfig = cacheConfig;
        embeddedCacheManager = new DefaultCacheManager(cacheConfig.getGlobalConfiguration());

        // Configure primary cache
        if (cacheConfig.isPrimaryCacheEnabled()) {
            embeddedCacheManager.defineConfiguration(
                cacheConfig.getPrimaryCacheName(),
                cacheConfig.getPrimaryCacheConfiguration()
            );
        }

        // Configure secondary cache
        if (cacheConfig.isSecondaryCacheEnabled()) {
            embeddedCacheManager.defineConfiguration(
                cacheConfig.getSecondaryCacheName(),
                cacheConfig.getSecondaryCacheConfiguration()
            );
        }
    }

    @Bean
    EmbeddedCacheManager embeddedCacheManager() {
        return embeddedCacheManager;
    }

    @Bean
    public AdvancedCache<String, MemcachedItem> mainCache(EmbeddedCacheManager embeddedCacheManager) {
        if (!cacheConfig.isPrimaryCacheEnabled()) {
            return null;
        }

        Cache<String, MemcachedItem> cache = embeddedCacheManager.getCache(cacheConfig.getPrimaryCacheName());
        return cache.getAdvancedCache();
    }

    @Bean
    public AdvancedCache<String, MemcachedItem> sessionCache(EmbeddedCacheManager embeddedCacheManager) {
        if (!cacheConfig.isSecondaryCacheEnabled()) {
            return null;
        }

        Cache<String, MemcachedItem> cache = embeddedCacheManager.getCache(cacheConfig.getSecondaryCacheName());
        return cache.getAdvancedCache();
    }

}
