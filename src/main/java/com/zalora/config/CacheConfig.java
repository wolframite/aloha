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
    private Configuration cacheConfiguration;

    @Getter
    @Value("${infinispan.cache.name}")
    private String cacheName;

    @Value("${infinispan.cache.mode}")
    private CacheMode cacheMode;

    @Value("${infinispan.cluster.name}")
    private String clusterName;

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

        cacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(cacheMode)
            .build();
    }

    private boolean isDev() {
        return env.acceptsProfiles("dev", "default");
    }

}
