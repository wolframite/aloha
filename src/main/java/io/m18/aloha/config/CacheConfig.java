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
    private Configuration primaryCacheConfiguration;

    @Getter
    private Configuration secondaryCacheConfiguration;

    // General cluster configuration
    @Value("${infinispan.cluster.name}")
    private String clusterName;

    @Getter
    @Value("${infinispan.cluster.statistics.enabled}")
    private boolean globalStatisticsEnabled;

    @Value("${infinispan.cluster.jgroups.config}")
    private String jgroupsConfig;

    // Primary cache configuration
    @Getter
    @Value("${infinispan.cache.primary.name}")
    private String primaryCacheName;

    @Value("${infinispan.cache.primary.mode}")
    private CacheMode primaryCacheMode;

    @Value("${infinispan.cache.primary.numOwners}")
    private int primaryCacheNumOwners;

    @Value("${infinispan.cache.primary.lock.timeout}")
    private int primaryCacheLockTimeout;

    @Value("${infinispan.cache.primary.lock.concurrency}")
    private int primaryCacheLockConcurrency;

    @Getter
    @Value("${infinispan.cache.primary.enabled}")
    private boolean primaryCacheEnabled;

    @Value("${infinispan.cache.primary.stateTransferChunkSize}")
    private int primaryStateTransferChunkSize;

    // Secondary cache configuration
    @Getter
    @Value("${infinispan.cache.secondary.name}")
    private String secondaryCacheName;

    @Value("${infinispan.cache.secondary.mode}")
    private CacheMode secondaryCacheMode;

    @Value("${infinispan.cache.secondary.numOwners}")
    private int secondaryCacheNumOwners;

    @Value("${infinispan.cache.secondary.lock.timeout}")
    private int secondaryCacheLockTimeout;

    @Value("${infinispan.cache.secondary.lock.concurrency}")
    private int secondaryCacheLockConcurrency;

    @Getter
    @Value("${infinispan.cache.secondary.enabled}")
    private boolean secondaryCacheEnabled;

    @Value("${infinispan.cache.secondary.stateTransferChunkSize}")
    private int secondaryStateTransferChunkSize;

    @PostConstruct
    public void init() {
        GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();
        gcb.transport()
            .defaultTransport()
            .clusterName(clusterName)
            .globalJmxStatistics().enabled(globalStatisticsEnabled);

        gcb.shutdown().hookBehavior(ShutdownHookBehavior.REGISTER);

        if (!jgroupsConfig.equals("")) {
            gcb.transport().addProperty("configurationFile", jgroupsConfig);
        }

        globalConfiguration = gcb.build();

        configurePrimaryCache();
        configureSecondaryCache();
    }

    private void configurePrimaryCache() {
        if (!isPrimaryCacheEnabled()) {
            return;
        }

        ConfigurationBuilder primaryCacheConfigurationBuilder = new ConfigurationBuilder();

        primaryCacheConfigurationBuilder
            .clustering().cacheMode(primaryCacheMode)
            .stateTransfer().chunkSize(primaryStateTransferChunkSize)
            .locking()
                .lockAcquisitionTimeout(primaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(primaryCacheLockConcurrency)
                .jmxStatistics().enable();

        if (primaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            primaryCacheConfigurationBuilder.clustering().hash().numOwners(primaryCacheNumOwners);
        }

        primaryCacheConfiguration = primaryCacheConfigurationBuilder.build();
    }

    private void configureSecondaryCache() {
        if (!isSecondaryCacheEnabled()) {
            return;
        }

        ConfigurationBuilder secondaryCacheConfigurationBuilder = new ConfigurationBuilder();
        secondaryCacheConfigurationBuilder
            .clustering().cacheMode(secondaryCacheMode)
            .locking()
                .lockAcquisitionTimeout(secondaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(secondaryCacheLockConcurrency)
            .jmxStatistics().enable();

        if (secondaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            secondaryCacheConfigurationBuilder.clustering().hash().numOwners(secondaryCacheNumOwners);
        }

        secondaryCacheConfiguration = secondaryCacheConfigurationBuilder.build();
    }

}