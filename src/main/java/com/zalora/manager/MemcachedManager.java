package com.zalora.manager;

import javax.annotation.PostConstruct;
import com.zalora.jmemcached.CacheImpl;
import org.springframework.util.Assert;
import com.zalora.config.MemcachedConfig;
import com.zalora.jmemcached.MemCacheDaemon;
import com.zalora.storage.MainInfiniBridge;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class MemcachedManager {

    private CacheManager cacheManager;
    private MemcachedConfig memcachedConfig;
    private MainInfiniBridge mainInfiniBridge;

    @Autowired
    public MemcachedManager(
        CacheManager cacheManager,
        MemcachedConfig memcachedConfig,
        MainInfiniBridge mainInfiniBridge) {

        Assert.notNull(cacheManager, "Infinispan Cache Manager could not be loaded");
        Assert.notNull(mainInfiniBridge, "Product Infinispan Bridge Storage could not be loaded");
        Assert.notNull(
            memcachedConfig.getInetSocketAddress(),
            "Product-Memcached listen address is not configured"
        );

        this.cacheManager = cacheManager;
        this.memcachedConfig = memcachedConfig;
        this.mainInfiniBridge = mainInfiniBridge;
    }

    @PostConstruct
    public void init() {
        MemCacheDaemon productDaemon = new MemCacheDaemon();
        productDaemon.setAddr(memcachedConfig.getInetSocketAddress());
        productDaemon.setIdleTime(memcachedConfig.getIdleTime());
        productDaemon.setVerbose(memcachedConfig.isVerbose());
        productDaemon.setCache(new CacheImpl(mainInfiniBridge));
        productDaemon.start();
    }

}
