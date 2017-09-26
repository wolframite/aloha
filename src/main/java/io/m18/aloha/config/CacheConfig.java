package io.m18.aloha.config;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class CacheConfig {

    private static final String CACHE_MODE_DISTRIBUTED = "DISTRIBUTED";
    private static final String CACHE_MODE_LOCAL = "LOCAL";

    private GlobalConfiguration globalConfiguration;
    private Configuration cacheConfiguration;

    // General cluster configuration
    @Value("${infinispan.cluster.name}")
    private String clusterName;

    @Value("${infinispan.cluster.jgroups}")
    private String jgroups;

    // Cache configuration
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

    @PostConstruct
    public void init() {
        GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();

        if (!cacheMode.equals(CacheMode.LOCAL)) {
            gcb.transport().defaultTransport()
                .clusterName(clusterName);
        }

        globalConfiguration = gcb.globalJmxStatistics().enable()
            .shutdown().hookBehavior(ShutdownHookBehavior.REGISTER)
            .build();

        configureCache();
    }

    @Bean
    public GlobalConfiguration globalConfiguration() {
        return globalConfiguration;
    }

    @Bean
    public Configuration cacheConfiguration() {
        return cacheConfiguration;
    }

    private void configureCache() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder
            .jmxStatistics().enable();

        if (!cacheMode.equals(CacheMode.LOCAL)) {
            configurationBuilder
            .clustering().cacheMode(cacheMode)
                .stateTransfer().chunkSize(stateTransferChunkSize)
            .locking()
                .lockAcquisitionTimeout(lockTimeout, TimeUnit.SECONDS)
                .concurrencyLevel(lockConcurrency);

            if (cacheMode.friendlyCacheModeString().equals(CACHE_MODE_DISTRIBUTED)) {
                configurationBuilder.clustering().hash().numOwners(numOwners);
            }
        }

        cacheConfiguration = configurationBuilder.build();
    }

}
