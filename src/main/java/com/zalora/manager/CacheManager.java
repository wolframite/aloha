package com.zalora.manager;

import lombok.Getter;
import org.infinispan.Cache;
import org.infinispan.manager.*;
import org.infinispan.AdvancedCache;
import com.zalora.config.CacheConfig;
import javax.annotation.PostConstruct;
import org.springframework.util.Assert;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class CacheManager {

    private CacheConfig cacheConfig;

    @Getter
    private EmbeddedCacheManager embeddedCacheManager;

    @Autowired
    public CacheManager(CacheConfig cacheConfig) {
        Assert.notNull(cacheConfig, "Configuration could not be loaded");
        this.cacheConfig = cacheConfig;
    }

    @PostConstruct
    public void init() {
        embeddedCacheManager = new DefaultCacheManager(cacheConfig.getGlobalConfiguration());
        embeddedCacheManager.defineConfiguration(
            cacheConfig.getCacheName(),
            cacheConfig.getCacheConfiguration()
        );
        embeddedCacheManager.startCaches(cacheConfig.getCacheName());
    }

    public AdvancedCache<String, byte[]> getProductStorage() {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getCacheName());
        return cache.getAdvancedCache();
    }

}
