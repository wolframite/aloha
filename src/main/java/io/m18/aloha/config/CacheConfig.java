package io.m18.aloha.config;

import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.ShutdownHookBehavior;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class CacheConfig {

    private static final String CACHE_MODE_DISTRIBUTED = "DISTRIBUTED";

    @Getter
    private GlobalConfiguration globalConfiguration;

    @Getter
    private Configuration cacheConfiguration;

    // General cluster configuration
    @Value("${infinispan.cluster.name}")
    private String clusterName;

    // Cache configuration
    @Getter
    @Value("${infinispan.cache.name}")
    private String cacheName;

    @Value("${infinispan.cache.mode}")
    private CacheMode cacheMode;

    @Value("${infinispan.cache.numOwners}")
    private int numOwners;

    @Value("${infinispan.cache.lock.timeout}")
    private int lockTimeout;

    @Value("${infinispan.cache.lock.concurrency}")
    private int lockConcurrency;

    @Value("${infinispan.cache.stateTransferChunkSize}")
    private int stateTransferChunkSize;

    private boolean compatibility;

    @PostConstruct
    public void init() {
        GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();
        gcb.transport()
            .defaultTransport()
            .clusterName(clusterName)
            .globalJmxStatistics().enable();

        gcb.shutdown().hookBehavior(ShutdownHookBehavior.REGISTER);
        globalConfiguration = gcb.build();

        configurePrimaryCache();
    }

    private void configurePrimaryCache() {
        ConfigurationBuilder primaryCacheConfigurationBuilder = new ConfigurationBuilder();

        primaryCacheConfigurationBuilder
            .compatibility().enabled(compatibility)
            .clustering().cacheMode(cacheMode)
                .stateTransfer().chunkSize(stateTransferChunkSize)
            .locking()
                .lockAcquisitionTimeout(lockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(lockConcurrency)
            .jmxStatistics().enable();

        if (cacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            primaryCacheConfigurationBuilder.clustering().hash().numOwners(numOwners);
        }

        cacheConfiguration = primaryCacheConfigurationBuilder.build();
    }

}
