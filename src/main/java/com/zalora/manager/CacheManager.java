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
            cacheConfig.getMainCacheName(),
            cacheConfig.getMainConfiguration()
        );

        embeddedCacheManager.defineConfiguration(
            cacheConfig.getProductCacheName(),
            cacheConfig.getProductConfiguration()
        );

        embeddedCacheManager.defineConfiguration(
            cacheConfig.getSessionCacheName(),
            cacheConfig.getSessionConfiguration()
        );

        embeddedCacheManager.startCaches(
            cacheConfig.getMainCacheName(),
            cacheConfig.getProductCacheName(),
            cacheConfig.getSessionCacheName()
        );
    }

    public AdvancedCache<byte[], byte[]> getMainStorage() {
        Cache<byte[], byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getMainCacheName());
        return cache.getAdvancedCache();
    }

    public AdvancedCache<byte[], byte[]> getProductStorage() {
        Cache<byte[], byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getProductCacheName());
        return cache.getAdvancedCache();
    }

    public AdvancedCache<byte[], byte[]> getSessionStorage() {
        Cache<byte[], byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getSessionCacheName());
        return cache.getAdvancedCache();
    }

}
