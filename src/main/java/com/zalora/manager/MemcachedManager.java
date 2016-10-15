package com.zalora.manager;

import javax.annotation.PostConstruct;

import com.zalora.storage.MainInfiniBridge;
import com.zalora.storage.SessionInfiniBridge;
import org.springframework.util.Assert;
import com.zalora.storage.AbstractInfiniBridge;
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
    private MainInfiniBridge mainInfiniBridge;
    private SessionInfiniBridge sessionInfiniBridge;

    @Autowired
    public MemcachedManager(
            CacheManager cacheManager,
            MemcachedConfig memcachedConfig,
            SessionInfiniBridge sessionInfiniBridge,
            MainInfiniBridge mainInfiniBridge
            ) {
        Assert.notNull(cacheManager, "Infinispan Cache Manager could not be loaded");
        Assert.notNull(memcachedConfig.getMainInetSocketAddress(), "Main-Memcached listen address is not configured");
        Assert.notNull(memcachedConfig.getSessionInetSocketAddress(), "Session-Memcached listen address is not configured");
        Assert.notNull(mainInfiniBridge, "Main Infinispan Bridge Storage could not be loaded");
        Assert.notNull(sessionInfiniBridge, "Session Infinispan Bridge Storage could not be loaded");

        this.cacheManager = cacheManager;
        this.memcachedConfig = memcachedConfig;
        this.mainInfiniBridge = mainInfiniBridge;
        this.sessionInfiniBridge = sessionInfiniBridge;
    }

    @PostConstruct
    public void init() {
        MemCacheDaemon mainDaemon = new MemCacheDaemon();
        mainDaemon.setAddr(memcachedConfig.getMainInetSocketAddress());
        mainDaemon.setIdleTime(memcachedConfig.getIdleTime());
        mainDaemon.setVerbose(memcachedConfig.isVerbose());
        mainDaemon.setCache(new CacheImpl(mainInfiniBridge));
        mainDaemon.start();

        MemCacheDaemon sessionDaemon = new MemCacheDaemon();
        sessionDaemon.setAddr(memcachedConfig.getSessionInetSocketAddress());
        sessionDaemon.setIdleTime(memcachedConfig.getIdleTime());
        sessionDaemon.setVerbose(memcachedConfig.isVerbose());
        sessionDaemon.setCache(new CacheImpl(sessionInfiniBridge));
        sessionDaemon.start();
    }

}
