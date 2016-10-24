package com.zalora.aloha.manager;

import com.zalora.aloha.config.CacheConfig;
import com.zalora.aloha.loader.Preloader;
import com.zalora.aloha.models.entities.Item;
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

    private CacheConfig cacheConfig;

    @Getter
    private EmbeddedCacheManager embeddedCacheManager;

    private Preloader preloader;

    @Autowired
    public CacheManager(CacheConfig cacheConfig, Preloader preloader) {
        Assert.notNull(cacheConfig, "Configuration could not be loaded");
        Assert.notNull(preloader, "Pre-Loader could not be loaded");

        this.cacheConfig = cacheConfig;
        this.preloader = preloader;
    }

    @PostConstruct
    public void init() {
        // At least one cache should be running
        Assert.isTrue(
            cacheConfig.isPrimaryCacheEnabled() ||
            cacheConfig.isSecondaryCacheEnabled() ||
            cacheConfig.isReadthroughCacheEnabled()
        );

        String[] enabledCaches = new String[]{"", "", ""};
        embeddedCacheManager = new DefaultCacheManager(cacheConfig.getGlobalConfiguration());

        // Configure primary cache
        if (cacheConfig.isPrimaryCacheEnabled()) {
            embeddedCacheManager.defineConfiguration(
                cacheConfig.getPrimaryCacheName(),
                cacheConfig.getPrimaryCacheConfiguration()
            );

            enabledCaches[0] = cacheConfig.getPrimaryCacheName();
        }

        // Configure secondary cache
        if (cacheConfig.isSecondaryCacheEnabled()) {
            embeddedCacheManager.defineConfiguration(
                cacheConfig.getSecondaryCacheName(),
                cacheConfig.getSecondaryCacheConfiguration()
            );

            enabledCaches[1] = cacheConfig.getSecondaryCacheName();
        }

        // Configure read through cache
        if (cacheConfig.isReadthroughCacheEnabled()) {
            embeddedCacheManager.defineConfiguration(
                cacheConfig.getReadthroughCacheName(),
                cacheConfig.getReadthroughCacheConfiguration()
            );

            // Preload items
            preloader.preLoad(cacheConfig.isPreload(), getReadthroughCache());

            enabledCaches[2] = cacheConfig.getReadthroughCacheName();
        }

        embeddedCacheManager.startCaches(enabledCaches);
    }

    public AdvancedCache<String, byte[]> getPrimaryCache() {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getPrimaryCacheName());
        return cache.getAdvancedCache();
    }

    public AdvancedCache<String, byte[]> getSecondaryCache() {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheConfig.getSecondaryCacheName());
        return cache.getAdvancedCache();
    }

    public AdvancedCache<String, Item> getReadthroughCache() {
        Cache<String, Item> cache = embeddedCacheManager.getCache(cacheConfig.getReadthroughCacheName());
        return cache.getAdvancedCache();
    }

}
