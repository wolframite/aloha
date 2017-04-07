package com.zalora.aloha.config;

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
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.persistence.rocksdb.configuration.RocksDBStoreConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class CacheConfig {

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

    @Getter
    @Value("${infinispan.cache.primary.statistics.enabled}")
    private boolean primaryStatisticsEnabled;

    @Value("${infinispan.cache.primary.persistence}")
    private boolean primaryPersistenceEnabled;

    @Value("${infinispan.cache.primary.stateTransferChunkSize}")
    private int primaryStateTransferChunkSize;

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

    @Getter
    @Value("${infinispan.cache.secondary.statistics.enabled}")
    private boolean secondaryStatisticsEnabled;

    @Value("${infinispan.cache.secondary.stateTransferChunkSize}")
    private int secondaryStateTransferChunkSize;

    @Value("${infinispan.cluster.jgroups.config}")
    private String jgroupsConfig;


    // Passivation configuration
    @Value("${infinispan.cache.primary.passivation.enabled}")
    private boolean primaryPassivationEnabled;

    @Value("${infinispan.cache.secondary.passivation.enabled}")
    private boolean secondaryPassivationEnabled;

    @Value("${infinispan.cache.primary.passivation.dataLocation}")
    private String primaryDataLocation;

    @Value("${infinispan.cache.primary.passivation.expiredLocation}")
    private String primaryExpiredLocation;

    @Value("${infinispan.cache.primary.passivation.maxSize}")
    private int primaryMaxSize;

    @Value("${infinispan.cache.secondary.passivation.dataLocation}")
    private String secondaryDataLocation;

    @Value("${infinispan.cache.secondary.passivation.expiredLocation}")
    private String secondaryExpiredLocation;

    @Value("${infinispan.cache.secondary.passivation.maxSize}")
    private int secondaryMaxEntries;

    // L1 configuration
    @Value("${infinispan.cache.primary.l1.enabled}")
    private boolean primaryL1Enabled;

    @Value("${infinispan.cache.secondary.l1.enabled}")
    private boolean secondaryL1Enabled;

    @Value("${infinispan.cache.primary.l1.lifespan}")
    private long primaryL1Lifespan;

    @Value("${infinispan.cache.secondary.l1.lifespan}")
    private long secondaryL1Lifespan;

    @PostConstruct
    public void init() {
        configure();
    }

    private void configure() {
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
            .stateTransfer().chunkSize(128)
            .locking()
                .lockAcquisitionTimeout(primaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(primaryCacheLockConcurrency)
            .jmxStatistics().enabled(primaryStatisticsEnabled);

        if (primaryCacheMode == CacheMode.DIST_ASYNC || primaryCacheMode == CacheMode.DIST_SYNC) {
            primaryCacheConfigurationBuilder.clustering().hash().numOwners(primaryCacheNumOwners);
        }

        if (primaryL1Enabled) {
            primaryCacheConfigurationBuilder.clustering().l1()
                .enabled(true)
                .lifespan(primaryL1Lifespan, TimeUnit.SECONDS);
        }

        if (primaryPassivationEnabled || primaryPersistenceEnabled) {
            primaryCacheConfigurationBuilder.persistence()
                .passivation(primaryPassivationEnabled)
                .addStore(RocksDBStoreConfigurationBuilder.class)
                    .location(primaryDataLocation)
                    .expiredLocation(primaryExpiredLocation)
                    .purgeOnStartup(true)

                .eviction()
                    .strategy(EvictionStrategy.LRU)
                    .size(primaryMaxSize).type(EvictionType.MEMORY);

            if (primaryPersistenceEnabled) {
                primaryCacheConfigurationBuilder.persistence()
                    .passivation(false)
                    .stores().get(0).purgeOnStartup(false);
            }
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
            .jmxStatistics().enabled(secondaryStatisticsEnabled);

        if (secondaryCacheMode == CacheMode.DIST_ASYNC || secondaryCacheMode == CacheMode.DIST_SYNC) {
            secondaryCacheConfigurationBuilder.clustering().hash().numOwners(secondaryCacheNumOwners);
        }

        if (secondaryL1Enabled) {
            secondaryCacheConfigurationBuilder.clustering().l1()
                .enabled(true)
                .lifespan(secondaryL1Lifespan, TimeUnit.SECONDS);
        }

        if (secondaryPassivationEnabled) {
            secondaryCacheConfigurationBuilder.persistence()
                .passivation(true)
                    .addStore(RocksDBStoreConfigurationBuilder.class)
                    .location(secondaryDataLocation)
                    .expiredLocation(secondaryExpiredLocation)
                    .purgeOnStartup(true)

                .eviction()
                    .strategy(EvictionStrategy.LIRS)
                    .size(secondaryMaxEntries).type(EvictionType.COUNT);
        }

        secondaryCacheConfiguration = secondaryCacheConfigurationBuilder.build();
    }

}
