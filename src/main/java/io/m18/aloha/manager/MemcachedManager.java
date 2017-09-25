package io.m18.aloha.manager;

import io.m18.aloha.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.server.memcached.MemcachedServer;
import org.infinispan.server.memcached.configuration.MemcachedServerConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class MemcachedManager {

    @Autowired
    private CacheConfig cacheConfig;

    @Autowired
    private EmbeddedCacheManager embeddedCacheManager;

    @PostConstruct
    public void init() {
        MemcachedServer memcachedServer = new MemcachedServer();
        memcachedServer.start(new MemcachedServerConfigurationBuilder().build(), embeddedCacheManager);
    }

}
