package com.zalora.manager;

import lombok.Getter;
import org.infinispan.Cache;
import org.infinispan.manager.*;
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
    private EmbeddedCacheManager cacheManager;

    @Autowired
    public CacheManager(CacheConfig cacheConfig) {
        Assert.notNull(cacheConfig, "Configuration could not be loaded");
        this.cacheConfig = cacheConfig;
    }

    @PostConstruct
    public void init() {
        cacheManager = new DefaultCacheManager(cacheConfig.getGlobalConfiguration(), cacheConfig.getConfiguration());
        cacheManager.startCaches(cacheConfig.getCacheName());
    }

    public Cache<String, String> getMainStorageCache() {
        return cacheManager.getCache(cacheConfig.getCacheName());
    }
}
