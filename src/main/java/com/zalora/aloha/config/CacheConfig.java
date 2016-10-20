package com.zalora.aloha.config;

import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.persistence.jpa.configuration.JpaStoreConfigurationBuilder;
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

    @Value("${infinispan.cluster.name}")
    private String clusterName;

    @Getter
    @Value("${infinispan.cache.primary.name}")
    private String primaryCacheName;

    @Value("${infinispan.cache.primary.mode}")
    private CacheMode primaryCacheMode;

    @Getter
    @Value("${infinispan.cache.primary.enabled}")
    private boolean primaryCacheEnabled;

    @Getter
    @Value("${infinispan.cache.secondary.name}")
    private String secondaryCacheName;

    @Value("${infinispan.cache.secondary.mode}")
    private CacheMode secondaryCacheMode;

    @Getter
    @Value("${infinispan.cache.secondary.enabled}")
    private boolean secondaryCacheEnabled;

    @Getter
    @Value("${infinispan.cache.readthrough.name}")
    private String readthroughCacheName;

    @Value("${infinispan.cache.readthrough.mode}")
    private CacheMode readthroughCacheMode;

    @Getter
    @Value("${infinispan.cache.readthrough.enabled}")
    private boolean readthroughCacheEnabled;

    @Getter
    @Value("${infinispan.cache.readthrough.preload}")
    private boolean preload;

    @Value("${infinispan.cache.readthrough.entityClass}")
    private String entityClassName;

    @Value("${infinispan.cache.readthrough.persistenceUnitName}")
    private String persistenceUnitName;

    @Value("${infinispan.cluster.jgroups.config}")
    private String jgroupsConfig;

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
            .clusterName(clusterName);

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

        primaryCacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(primaryCacheMode)
            .build();
    }

    private void configureSecondaryCache() {
        if (!isSecondaryCacheEnabled()) {
            return;
        }

        secondaryCacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(secondaryCacheMode)
            .build();
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

        readthroughCacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(readthroughCacheMode)
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
                    .ignoreModifications(true)
            .build();
    }

}
