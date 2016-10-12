package com.zalora.config;

import lombok.Getter;
import javax.annotation.PostConstruct;
import org.infinispan.configuration.cache.*;
import org.infinispan.configuration.global.*;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.*;

/**
 * @author Wolfram Huesken
 */
@Component
public class CacheConfig {

    @Autowired
    Environment env;

    @Getter
    private Configuration configuration;

    @Getter
    private GlobalConfiguration globalConfiguration;

    @Getter
    @Value("${infinispan.cache.name}")
    private String cacheName;

    @Value("${infinispan.cluster.name}")
    private String clusterName;

    @PostConstruct
    public void init() { configure(); }

    private final void configure() {
        configuration = new ConfigurationBuilder()
            .clustering().cacheMode(CacheMode.REPL_ASYNC)
            .build();

        GlobalConfigurationBuilder gcb = new GlobalConfigurationBuilder();
        gcb.transport()
            .defaultTransport()
            .clusterName(clusterName);

        if (!isDev()) {
            gcb.transport().addProperty("configurationFile", "jgroups.config.xml");

            System.setProperty("jgroups.s3.access_key", System.getenv("S3_ACCESS_KEY"));
            System.setProperty("jgroups.s3.secret_access_key", System.getenv("S3_SECRET_KEY"));
            System.setProperty("jgroups.s3.bucket", System.getenv("S3_BUCKET"));
        }

        globalConfiguration = gcb.build();
    }

    private boolean isDev() {
        return env.acceptsProfiles("dev", "default");
    }
}
