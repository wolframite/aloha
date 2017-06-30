package io.m18.aloha.manager;

import io.m18.aloha.compressor.Compressor;
import io.m18.aloha.compressor.NoCompressor;
import io.m18.aloha.config.CacheConfig;
import io.m18.aloha.config.MemcachedConfig;
import io.m18.aloha.memcached.MemcachedItem;
import io.m18.aloha.storage.DefaultInfiniBridge;
import io.m18.jmemcached.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class MemcachedManager {

    private static final String DEFAULT_COMPRESSOR = "com.zalora.aloha.compressor.NoCompressor";

    @Autowired
    private CacheConfig cacheConfig;

    @Autowired
    private MemcachedConfig memcachedConfig;

    @Autowired
    private InetSocketAddress mainSocketAddress;

    @Autowired
    private InetSocketAddress sessionSocketAddress;

    @Autowired
    private AdvancedCache<String, MemcachedItem> mainCache;

    @Autowired
    private AdvancedCache<String, MemcachedItem> sessionCache;

    @Value("${infinispan.cache.primary.compressor}")
    private String primaryCompressorClass;

    @Value("${infinispan.cache.secondary.compressor}")
    private String secondaryCompressorClass;

    private Compressor primaryCompressor;
    private Compressor secondaryCompressor;

    @Getter
    private MemCacheDaemon<LocalCacheElement> mainMemcachedDaemon;

    @Getter
    private MemCacheDaemon<LocalCacheElement> sessionMemcachedDaemon;

    @PostConstruct
    public void init() {
        initCompressors();

        if (cacheConfig.isPrimaryCacheEnabled()) {
            mainMemcachedDaemon = new MemCacheDaemon<>();
            mainMemcachedDaemon.setAddr(mainSocketAddress);
            mainMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            mainMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            mainMemcachedDaemon.setCache(new CacheImpl(new DefaultInfiniBridge(mainCache, primaryCompressor)));

            mainMemcachedDaemon.start();
        }

        if (cacheConfig.isSecondaryCacheEnabled()) {
            sessionMemcachedDaemon = new MemCacheDaemon<>();
            sessionMemcachedDaemon.setAddr(sessionSocketAddress);
            sessionMemcachedDaemon.setIdleTime(memcachedConfig.getIdleTime());
            sessionMemcachedDaemon.setVerbose(memcachedConfig.isVerbose());
            sessionMemcachedDaemon.setCache(new CacheImpl(new DefaultInfiniBridge(sessionCache, secondaryCompressor)));

            sessionMemcachedDaemon.start();
        }
    }

    private void initCompressors() {
        if (primaryCompressorClass.isEmpty()) {
            primaryCompressorClass = DEFAULT_COMPRESSOR;
        }

        if (secondaryCompressorClass.isEmpty()) {
            secondaryCompressorClass = DEFAULT_COMPRESSOR;
        }

        try {
            primaryCompressor = (Compressor) Class.forName(primaryCompressorClass).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            log.error("Could not instantiate {}, falling back to no compression", primaryCompressorClass);
            primaryCompressor = new NoCompressor();
        }

        try {
            secondaryCompressor = (Compressor) Class.forName(secondaryCompressorClass).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            log.error("Could not instantiate {}, falling back to no compression", secondaryCompressorClass);
            secondaryCompressor = new NoCompressor();
        }

        log.info("Primary Compressor: {}", primaryCompressor.getClass().getSimpleName());
        log.info("Secondary Compressor: {}", secondaryCompressor.getClass().getSimpleName());
    }

}
