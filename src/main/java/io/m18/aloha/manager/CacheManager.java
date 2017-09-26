package io.m18.aloha.manager;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.*;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class CacheManager {

    @Value("${infinispan.cache.name}")
    private String cacheName;

    @Autowired
    private GlobalConfiguration globalConfiguration;

    @Autowired
    private Configuration cacheConfiguration;

    private EmbeddedCacheManager embeddedCacheManager;

    @PostConstruct
    public void init() {
        embeddedCacheManager = new DefaultCacheManager(globalConfiguration);
        embeddedCacheManager.defineConfiguration(
            cacheName, cacheConfiguration
        );
    }

    @Bean
    EmbeddedCacheManager embeddedCacheManager() {
        return embeddedCacheManager;
    }

    @Bean
    public AdvancedCache<String, byte[]> mainCache(EmbeddedCacheManager embeddedCacheManager) {
        Cache<String, byte[]> cache = embeddedCacheManager.getCache(cacheName);
        return cache.getAdvancedCache();
    }

}
