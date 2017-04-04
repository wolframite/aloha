package com.zalora.aloha.manager;

import com.zalora.aloha.config.CacheConfig;
import com.zalora.aloha.config.MemcachedConfig;
import com.zalora.aloha.storage.DefaultInfiniBridge;
import com.zalora.jmemcached.CacheImpl;
import com.zalora.jmemcached.LocalCacheElement;
import com.zalora.jmemcached.MemCacheDaemon;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class MemcachedManager {

    private CacheConfig cacheConfig;
    private CacheManager cacheManager;
    private MemcachedConfig memcachedConfig;

    @Autowired
    public MemcachedManager(
        CacheManager cacheManager,
        CacheConfig cacheConfig,
        MemcachedConfig memcachedConfig) {

        Assert.notNull(cacheManager, "Infinispan Cache Manager could not be loaded");
        Assert.notNull(cacheConfig, "Infinispan Cache Configuration could not be loaded");
        Assert.notNull(
            memcachedConfig.getPrimaryInetSocketAddress(),
            "Main Memcached listen address is not configured"
        );

        this.cacheConfig = cacheConfig;
        this.cacheManager = cacheManager;
        this.memcachedConfig = memcachedConfig;
    }

    @PostConstruct
    public void init() {
        if (cacheConfig.isPrimaryCacheEnabled()) {
            MemCacheDaemon<LocalCacheElement> mainMemcachedDaemon = new MemCacheDaemon<>();
            mainMemcachedDaemon.setAddr(memcachedConfig.getPrimaryInetSocketAddress());
            mainMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            mainMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            mainMemcachedDaemon.setCache(
                new CacheImpl(new DefaultInfiniBridge(cacheManager.getPrimaryCache())
            ));

            mainMemcachedDaemon.start();
        }

        if (cacheConfig.isSecondaryCacheEnabled()) {
            MemCacheDaemon<LocalCacheElement> productMemcachedDaemon = new MemCacheDaemon<>();
            productMemcachedDaemon.setAddr(memcachedConfig.getSecondaryInetSocketAddress());
            productMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            productMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            productMemcachedDaemon.setCache(
                new CacheImpl(new DefaultInfiniBridge(cacheManager.getSecondaryCache())
            ));

            productMemcachedDaemon.start();
        }
    }

}
