package com.zalora.config;

import javax.annotation.PostConstruct;
import lombok.Getter;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class CacheConfig {

    @Autowired
    Environment env;

    @Getter
    private GlobalConfiguration globalConfiguration;

    @Getter
    private Configuration mainCacheConfiguration;

    @Getter
    private Configuration productCacheConfiguration;

    @Getter
    private Configuration sessionCacheConfiguration;

    @Value("${infinispan.cluster.name}")
    private String clusterName;

    @Getter
    @Value("${infinispan.cache.main.name}")
    private String mainCacheName;

    @Value("${infinispan.cache.main.mode}")
    private CacheMode mainCacheMode;

    @Getter
    @Value("${infinispan.cache.main.enabled}")
    private boolean mainCacheEnabled;

    @Getter
    @Value("${infinispan.cache.product.name}")
    private String productCacheName;

    @Value("${infinispan.cache.product.mode}")
    private CacheMode productCacheMode;

    @Getter
    @Value("${infinispan.cache.product.enabled}")
    private boolean productCacheEnabled;

    @Getter
    @Value("${infinispan.cache.session.name}")
    private String sessionCacheName;

    @Value("${infinispan.cache.session.mode}")
    private CacheMode sessionCacheMode;

    @Getter
    @Value("${infinispan.cache.session.enabled}")
    private boolean sessionCacheEnabled;

    @PostConstruct
    public void init() {
        configure();
    }

    private void configure() {
        GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();
        gcb.transport()
            .defaultTransport()
            .clusterName(clusterName);

        if (!isDev()) {
            gcb.transport().addProperty("configurationFile", "jgroups.config.xml");
        }

        globalConfiguration = gcb.build();
        configureMainCache();
        configureProductCache();
        configureSessionCache();
    }

    private void configureMainCache() {
        if (!isMainCacheEnabled()) {
            return;
        }

        mainCacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(mainCacheMode)
            .build();
    }

    private void configureProductCache() {
        if (!isProductCacheEnabled()) {
            return;
        }

        productCacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(productCacheMode)
            .build();
    }

    private void configureSessionCache() {
        if (!isSessionCacheEnabled()) {
            return;
        }

        sessionCacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(sessionCacheMode)
            .build();
    }

    private boolean isDev() {
        return env.acceptsProfiles("dev", "default");
    }

}
