package com.zalora.aloha.manager;

import com.zalora.aloha.config.CacheConfig;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class CacheManager {

    @Getter
    private EmbeddedCacheManager embeddedCacheManager;

    private CacheConfig cacheConfig;

    @Autowired
    public CacheManager(CacheConfig cacheConfig) {
        Assert.notNull(cacheConfig, "Configuration could not be loaded");

        this.cacheConfig = cacheConfig;
    }

    @PostConstruct
    public void init() {
        Assert.isTrue(cacheConfig.isPrimaryCacheEnabled() || cacheConfig.isSecondaryCacheEnabled(),
            "At least one Cache must be enabled"
        );

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

    public AdvancedCache<String, byte[]> getPrimaryCache() {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getPrimaryCacheName());
        return cache.getAdvancedCache();
    }

    public AdvancedCache<String, byte[]> getSecondaryCache() {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getSecondaryCacheName());
        return cache.getAdvancedCache();
    }

}
