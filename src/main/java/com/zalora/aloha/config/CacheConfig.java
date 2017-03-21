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
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.infinispan.persistence.jpa.configuration.JpaStoreConfigurationBuilder;
import org.infinispan.persistence.leveldb.configuration.LevelDBStoreConfiguration;
import org.infinispan.persistence.leveldb.configuration.LevelDBStoreConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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

    @Getter
    private Configuration readthroughCacheConfiguration;

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

    @Getter
    @Value("${infinispan.cache.secondary.name}")
    private String secondaryCacheName;

    @Value("${infinispan.cache.secondary.mode}")
    private CacheMode secondaryCacheMode;

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

    @Getter
    @Value("${infinispan.cache.readthrough.name}")
    private String readthroughCacheName;

    @Value("${infinispan.cache.readthrough.mode}")
    private CacheMode readthroughCacheMode;

    @Value("${infinispan.cache.readthrough.lock.timeout}")
    private int readthroughCacheLockTimeout;

    @Value("${infinispan.cache.readthrough.lock.concurrency}")
    private int readthroughCacheLockConcurrency;

    @Getter
    @Value("${infinispan.cache.readthrough.enabled}")
    private boolean readthroughCacheEnabled;

    @Getter
    @Value("${infinispan.cache.readthrough.statistics.enabled}")
    private boolean readthroughStatisticsEnabled;

    @Getter
    @Value("${infinispan.cache.readthrough.preload}")
    private boolean preload;

    @Value("${infinispan.cache.readthrough.entityClass}")
    private String entityClassName;

    @Value("${infinispan.cache.readthrough.persistenceUnitName}")
    private String persistenceUnitName;

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

    @Value("${infinispan.cache.readthrough.l1.enabled}")
    private boolean readthroughL1Enabled;

    @Value("${infinispan.cache.readthrough.l1.lifespan}")
    private long primaryL1Lifespan;

    @Value("${infinispan.cache.readthrough.l1.lifespan}")
    private long secondaryL1Lifespan;

    @Value("${infinispan.cache.readthrough.l1.lifespan}")
    private long readthroughL1Lifespan;

    @Autowired
    public CacheConfig(PropertyConfigurator propertyConfigurator) {
        Assert.notNull(propertyConfigurator, "One PropertyConfigurator has to be implemented");
    }

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

        if (!jgroupsConfig.equals("")) {
            gcb.transport().addProperty("configurationFile", jgroupsConfig);
        }

        globalConfiguration = gcb.build();
        configurePrimaryCache();
        configureSecondaryCache();
        configureReadthroughCache();
    }

    private void configurePrimaryCache() {
        if (!isPrimaryCacheEnabled()) {
            return;
        }

        ConfigurationBuilder primaryCacheConfigurationBuilder = new ConfigurationBuilder();
        primaryCacheConfigurationBuilder
            .clustering().cacheMode(primaryCacheMode)
            .locking()
                .lockAcquisitionTimeout(primaryCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(primaryCacheLockConcurrency)
            .jmxStatistics().enabled(primaryStatisticsEnabled);

        if (primaryL1Enabled) {
            primaryCacheConfigurationBuilder.clustering().l1()
                .enabled(true)
                .lifespan(primaryL1Lifespan, TimeUnit.SECONDS);
        }

        if (primaryPassivationEnabled) {
            primaryCacheConfigurationBuilder.persistence()
                .passivation(true)
                    .addStore(LevelDBStoreConfigurationBuilder.class)
                    .location(primaryDataLocation)
                    .expiredLocation(primaryExpiredLocation)
                    .purgeOnStartup(true)

                .eviction()
                    .strategy(EvictionStrategy.LRU)
                    .size(primaryMaxSize).type(EvictionType.MEMORY);
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

        if (secondaryL1Enabled) {
            secondaryCacheConfigurationBuilder.clustering().l1()
                .enabled(true)
                .lifespan(secondaryL1Lifespan, TimeUnit.SECONDS);
        }

        if (secondaryPassivationEnabled) {
            secondaryCacheConfigurationBuilder.persistence()
                .passivation(true)
                    .addStore(LevelDBStoreConfigurationBuilder.class)
                    .location(secondaryDataLocation)
                    .expiredLocation(secondaryExpiredLocation)
                    .purgeOnStartup(true)

                .eviction()
                    .strategy(EvictionStrategy.LIRS)
                    .size(secondaryMaxEntries).type(EvictionType.COUNT);
        }

        secondaryCacheConfiguration = secondaryCacheConfigurationBuilder.build();
    }

    private void configureReadthroughCache() {
        if (!isReadthroughCacheEnabled()) {
            return;
        }

        Class entityClass;

        try {
            entityClass = Class.forName(entityClassName);
        } catch (ClassNotFoundException ex) {
            log.error(
                "Entity class {} could not be found, read through cache is not configured properly",
                entityClassName, ex
            );
            return;
        }

        ConfigurationBuilder readthroughCacheConfigurationBuilder = new ConfigurationBuilder();
        readthroughCacheConfigurationBuilder
            .clustering().cacheMode(readthroughCacheMode)
            .locking()
                .lockAcquisitionTimeout(readthroughCacheLockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(readthroughCacheLockConcurrency)
            .jmxStatistics().enabled(readthroughStatisticsEnabled)
            .persistence()
                .passivation(false)
                .addStore(JpaStoreConfigurationBuilder.class)
                    .shared(false)
                    .preload(false)
                    .batchSize(1)
                    .persistenceUnitName(persistenceUnitName)
                    .storeMetadata(false)
                    .entityClass(entityClass)
                    .fetchPersistentState(false)
                    .purgeOnStartup(false)
                    .ignoreModifications(true);

        if (readthroughL1Enabled) {
            readthroughCacheConfigurationBuilder.clustering().l1()
                .enabled(true)
                .lifespan(readthroughL1Lifespan, TimeUnit.SECONDS);
        }

        readthroughCacheConfiguration = readthroughCacheConfigurationBuilder.build();
    }

}
