package com.zalora.manager;

import javax.annotation.PostConstruct;
import org.springframework.util.Assert;
import com.zalora.storage.InfiniBridge;
import com.zalora.config.MemcachedConfig;
import com.thimbleware.jmemcached.CacheImpl;
import org.springframework.stereotype.Component;
import com.thimbleware.jmemcached.MemCacheDaemon;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class MemcachedManager {

    private CacheManager cacheManager;
    private MemcachedConfig memcachedConfig;
    private InfiniBridge infiniBridge;

    @Autowired
    public MemcachedManager(CacheManager cacheManager, MemcachedConfig memcachedConfig, InfiniBridge infiniBridge) {
        Assert.notNull(cacheManager, "Infinispan Cache Manager could not be loaded");
        Assert.notNull(memcachedConfig.getInetSocketAddress(), "Memcached listen address is not configured");
        Assert.notNull(infiniBridge, "Infinispan Bridge Storage could not be loaded");

        this.cacheManager = cacheManager;
        this.memcachedConfig = memcachedConfig;
        this.infiniBridge = infiniBridge;
    }

    @PostConstruct
    public void init() {
        MemCacheDaemon daemon = new MemCacheDaemon();
        daemon.setAddr(memcachedConfig.getInetSocketAddress());
        daemon.setIdleTime(memcachedConfig.getIdleTime());
        daemon.setVerbose(memcachedConfig.isVerbose());
        daemon.setCache(new CacheImpl(infiniBridge));
        daemon.start();
    }

}
