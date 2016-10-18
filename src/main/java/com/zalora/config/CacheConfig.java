package com.zalora.config;

import lombok.Getter;
import javax.annotation.PostConstruct;
import org.springframework.util.Assert;
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
            setS3Credentials();

            // Overwrite clustering listen address
            String listenAddress = System.getenv("JGROUPS_INET_ADDRESS");
            if (listenAddress != null) {
                System.setProperty("jgroups.bind_addr", listenAddress);
            }

            gcb.transport().addProperty("configurationFile", "jgroups.config.xml");
        }

        globalConfiguration = gcb.build();

        cacheConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(CacheMode.DIST_ASYNC)
            .build();
    }

    private boolean isDev() {
        return env.acceptsProfiles("dev", "default");
    }

    private void setS3Credentials() {
        String accessKey = System.getenv("S3_ACCESS_KEY");
        String secretAccessKey = System.getenv("S3_SECRET_ACCESS_KEY");
        String bucket = System.getenv("S3_BUCKET");

        Assert.notNull(accessKey, "Access Key is not set (S3_ACCESS_KEY)");
        Assert.notNull(secretAccessKey, "Secret Access Key is not set (S3_SECRET_ACCESS_KEY)");
        Assert.notNull(bucket, "Bucket is not set (S3_BUCKET)");

        System.setProperty("jgroups.s3.access_key", accessKey);
        System.setProperty("jgroups.s3.secret_access_key", secretAccessKey);
        System.setProperty("jgroups.s3.bucket", bucket);
    }
}
