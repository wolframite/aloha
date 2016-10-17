package com.zalora.config;

import lombok.Getter;
import javax.annotation.PostConstruct;

import org.infinispan.commons.equivalence.Equivalence;
import org.infinispan.factories.components.ManageableComponentMetadata;
import org.infinispan.server.core.LifecycleCallbacks;
import org.infinispan.transaction.*;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;
import org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup;
import org.springframework.util.Assert;
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
    private GlobalConfiguration globalConfiguration;

    @Getter
    private Configuration mainConfiguration;

    @Getter
    private Configuration productConfiguration;

    @Getter
    private Configuration sessionConfiguration;

    @Getter
    @Value("${infinispan.cache.main.name}")
    private String mainCacheName;

    @Getter
    @Value("${infinispan.cache.product.name}")
    private String productCacheName;

    @Getter
    @Value("${infinispan.cache.session.name}")
    private String sessionCacheName;

    @Value("${infinispan.cluster.name}")
    private String clusterName;

    @PostConstruct
    public void init() { configure(); }

    private final void configure() {
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

        mainConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(CacheMode.DIST_ASYNC)
            .build();

        productConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(CacheMode.DIST_ASYNC)
            .build();

        sessionConfiguration = new ConfigurationBuilder()
            .clustering().cacheMode(CacheMode.REPL_SYNC)
            .versioning().
            .storeAsBinary().enabled(false)
            .transaction()
                .transactionMode(TransactionMode.TRANSACTIONAL)
                .transactionManagerLookup(new JBossStandaloneJTAManagerLookup())
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
