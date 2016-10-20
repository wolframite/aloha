package com.zalora.manager;

import com.zalora.config.CacheConfig;
import javax.annotation.PostConstruct;
import com.zalora.jmemcached.CacheImpl;
import org.springframework.util.Assert;
import com.zalora.config.MemcachedConfig;
import com.zalora.jmemcached.MemCacheDaemon;
import com.zalora.storage.DefaultInfiniBridge;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class MemcachedManager {

    private CacheConfig cacheConfig;
    private MemcachedConfig memcachedConfig;

    private DefaultInfiniBridge mainInfiniBridge;
    private DefaultInfiniBridge productInfiniBridge;
    private DefaultInfiniBridge sessionInfiniBridge;

    @Autowired
    public MemcachedManager(
        CacheManager cacheManager,
        CacheConfig cacheConfig,
        MemcachedConfig memcachedConfig) {

        Assert.notNull(cacheManager, "Infinispan Cache Manager could not be loaded");
        Assert.notNull(cacheConfig, "Infinispan Cache Configuration could not be loaded");
        Assert.notNull(
            memcachedConfig.getMainInetSocketAddress(),
            "Main Memcached listen address is not configured"
        );

        this.cacheConfig = cacheConfig;
        this.memcachedConfig = memcachedConfig;

        mainInfiniBridge = new DefaultInfiniBridge(cacheManager.getMainCache());
        productInfiniBridge = new DefaultInfiniBridge(cacheManager.getProductCache());
        sessionInfiniBridge = new DefaultInfiniBridge(cacheManager.getSessionCache());
    }

    @PostConstruct
    public void init() {
        if (cacheConfig.isMainCacheEnabled()) {
            MemCacheDaemon mainMemcachedDaemon = new MemCacheDaemon();
            mainMemcachedDaemon.setAddr(memcachedConfig.getMainInetSocketAddress());
            mainMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            mainMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            mainMemcachedDaemon.setCache(new CacheImpl(mainInfiniBridge));
            mainMemcachedDaemon.start();
        }

        if (cacheConfig.isProductCacheEnabled()) {
            MemCacheDaemon productMemcachedDaemon = new MemCacheDaemon();
            productMemcachedDaemon.setAddr(memcachedConfig.getProductInetSocketAddress());
            productMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            productMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            productMemcachedDaemon.setCache(new CacheImpl(productInfiniBridge));
            productMemcachedDaemon.start();
        }

        if (cacheConfig.isSessionCacheEnabled()) {
            MemCacheDaemon mainMemcachedDaemon = new MemCacheDaemon();
            mainMemcachedDaemon.setAddr(memcachedConfig.getSessionInetSocketAddress());
            mainMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            mainMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            mainMemcachedDaemon.setCache(new CacheImpl(sessionInfiniBridge));
            mainMemcachedDaemon.start();
        }
    }

}
