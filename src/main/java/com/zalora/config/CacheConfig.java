package com.zalora.config;

import lombok.Getter;
import javax.annotation.PostConstruct;
import org.infinispan.configuration.cache.*;
import org.infinispan.configuration.global.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.*;

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

    @PostConstruct
    public void init() { configure(); }

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

    private boolean isDev() {
        return env.acceptsProfiles("dev", "default");
    }

}
