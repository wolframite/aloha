package io.m18.aloha.manager;

import io.m18.aloha.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.server.memcached.MemcachedServer;
import org.infinispan.server.memcached.configuration.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class MemcachedManager {

    @Value("${memcached.enabled}")
    private boolean enabled;

    @Autowired
    private CacheConfig cacheConfig;

    @Autowired
    private MemcachedServerConfiguration memcachedServerConfiguration;

    @Autowired
    private EmbeddedCacheManager embeddedCacheManager;

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("Memcached is disabled");
            return;
        }

        MemcachedServer memcachedServer = new MemcachedServer();
        memcachedServer.start(memcachedServerConfiguration, embeddedCacheManager);

        log.info("Memcached is running on {}:{}",
            memcachedServerConfiguration.host(),
            memcachedServerConfiguration.port()
        );
    }

}
