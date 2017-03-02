package com.zalora.aloha.manager;

import com.zalora.jmemcached.CacheImpl;
import com.zalora.jmemcached.LocalCacheElement;
import com.zalora.jmemcached.MemCacheDaemon;
import com.zalora.aloha.config.CacheConfig;
import com.zalora.aloha.config.MemcachedConfig;
import com.zalora.aloha.storage.DefaultInfiniBridge;
import com.zalora.aloha.storage.ReadthroughInfiniBridge;
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
    private MemcachedConfig memcachedConfig;

    private DefaultInfiniBridge primaryInfiniBridge;
    private DefaultInfiniBridge secondaryInfiniBridge;
    private ReadthroughInfiniBridge readthroughInfiniBridge;

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
        this.memcachedConfig = memcachedConfig;

        if (cacheConfig.isPrimaryCacheEnabled()) {
            primaryInfiniBridge = new DefaultInfiniBridge(cacheManager.getPrimaryCache());
        }

        if (cacheConfig.isSecondaryCacheEnabled()) {
            secondaryInfiniBridge = new DefaultInfiniBridge(cacheManager.getSecondaryCache());
        }

        if (cacheConfig.isReadthroughCacheEnabled()) {
            readthroughInfiniBridge = new ReadthroughInfiniBridge(cacheManager.getReadthroughCache());
        }
    }

    @PostConstruct
    public void init() {
        if (cacheConfig.isPrimaryCacheEnabled()) {
            MemCacheDaemon<LocalCacheElement> mainMemcachedDaemon = new MemCacheDaemon<>();
            mainMemcachedDaemon.setAddr(memcachedConfig.getPrimaryInetSocketAddress());
            mainMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            mainMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            mainMemcachedDaemon.setCache(new CacheImpl(primaryInfiniBridge));
            mainMemcachedDaemon.start();
        }

        if (cacheConfig.isSecondaryCacheEnabled()) {
            MemCacheDaemon<LocalCacheElement> productMemcachedDaemon = new MemCacheDaemon<>();
            productMemcachedDaemon.setAddr(memcachedConfig.getSecondaryInetSocketAddress());
            productMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            productMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            productMemcachedDaemon.setCache(new CacheImpl(secondaryInfiniBridge));
            productMemcachedDaemon.start();
        }

        if (cacheConfig.isReadthroughCacheEnabled()) {
            MemCacheDaemon<LocalCacheElement> mainMemcachedDaemon = new MemCacheDaemon<>();
            mainMemcachedDaemon.setAddr(memcachedConfig.getReadthroughInetSocketAddress());
            mainMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            mainMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            mainMemcachedDaemon.setCache(new CacheImpl(readthroughInfiniBridge));
            mainMemcachedDaemon.start();
        }
    }

}
