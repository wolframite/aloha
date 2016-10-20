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

import java.util.ArrayList;
import java.util.List;

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
        // At least one cache should be running
        Assert.isTrue(cacheConfig.isMainCacheEnabled() || cacheConfig.isProductCacheEnabled());
        String[] enabledCaches = new String[]{"", ""};

        embeddedCacheManager = new DefaultCacheManager(cacheConfig.getGlobalConfiguration());

        // Configure main cache
        if (cacheConfig.isMainCacheEnabled()) {
            embeddedCacheManager.defineConfiguration(
                cacheConfig.getMainCacheName(),
                cacheConfig.getMainCacheConfiguration()
            );

            enabledCaches[0] = cacheConfig.getMainCacheName();
        }

        // Configure product cache
        if (cacheConfig.isProductCacheEnabled()) {
            embeddedCacheManager.defineConfiguration(
                cacheConfig.getProductCacheName(),
                cacheConfig.getProductCacheConfiguration()
            );

            enabledCaches[1] = cacheConfig.getProductCacheName();
        }

        embeddedCacheManager.startCaches(enabledCaches);
    }

    public AdvancedCache<String, byte[]> getMainCache() {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getMainCacheName());
        return cache.getAdvancedCache();
    }

    public AdvancedCache<String, byte[]> getProductCache() {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getProductCacheName());
        return cache.getAdvancedCache();
    }

}
