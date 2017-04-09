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
import org.infinispan.persistence.jdbc.configuration.JdbcStringBasedStoreConfigurationBuilder;
import org.infinispan.persistence.rocksdb.configuration.RocksDBStoreConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class CacheConfig {

    private static final String CACHE_MODE_DISTRIBUTED = "DISTRIBUTED";

    private static final String CACHE_MODE_INVALIDATED = "INVALIDATED";

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

    // Primary invalidation settings
    @Value("${infinispan.cache.primary.invalidationJdbc.connectionUrl}")
    private String primaryInvalidationConnectionUrl;

    @Value("${infinispan.cache.primary.invalidationJdbc.driverClass}")
    private String primaryInvalidationDriverClass;

    @Value("${infinispan.cache.primary.invalidationJdbc.username}")
    private String primaryInvalidationUsername;

    @Value("${infinispan.cache.primary.invalidationJdbc.password}")
    private String primaryInvalidationPassword;

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
            .stateTransfer().chunkSize(primaryStateTransferChunkSize)
            .locking()
                .lockAcquisitionTimeout(primaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(primaryCacheLockConcurrency)
                .jmxStatistics().enable();

        if (primaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            primaryCacheConfigurationBuilder.clustering().hash().numOwners(primaryCacheNumOwners);
        }

        if (primaryL1Enabled && primaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
            primaryCacheConfigurationBuilder.clustering().l1()
                .enabled(true)
                .lifespan(primaryL1Lifespan, TimeUnit.SECONDS);
        }

        if (primaryPassivationEnabled && !primaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_INVALIDATED)) {
            primaryCacheConfigurationBuilder.persistence()
                .passivation(primaryPassivationEnabled)
                .addStore(RocksDBStoreConfigurationBuilder.class)
                    .location(primaryDataLocation)
                    .expiredLocation(primaryExpiredLocation)
                    .purgeOnStartup(true)
                .eviction()
                    .strategy(EvictionStrategy.LRU)
                    .size(primaryMaxSize).type(EvictionType.MEMORY);
        }

        if (primaryCacheMode.friendlyCacheModeString().equals(CACHE_MODE_INVALIDATED)) {
            primaryCacheConfigurationBuilder.persistence().addStore(JdbcStringBasedStoreConfigurationBuilder.class)
                .preload(true)
                .shared(true)
                .table()
                    .createOnStart(true)
                    .tableNamePrefix("aloha")
                    .idColumnName("id").idColumnType("VARCHAR(255)")
                    .dataColumnName("data").dataColumnType("MEDIUMBLOB")
                    .timestampColumnName("expire").timestampColumnType("BIGINT")
                .connectionPool()
                    .connectionUrl(primaryInvalidationConnectionUrl)
                    .username(primaryInvalidationUsername)
                    .password(primaryInvalidationPassword)
                    .driverClass(primaryInvalidationDriverClass);
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
