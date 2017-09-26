package io.m18.aloha.config;

import org.infinispan.server.hotrod.configuration.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class HotRodConfig {

    @Value("${infinispan.cache.name}")
    private String cacheName;

    @Value("${hotrod.host}")
    private String networkAddress;

    @Value("${hotrod.topologyLockTimeout}")
    private int topologyLockTimeout;

    @Value("${hotrod.topologyReplTimeout}")
    private int topologyReplTimeout;

    @Bean
    public HotRodServerConfiguration hotRodServerConfiguration() {
        HotRodServerConfigurationBuilder builder = new HotRodServerConfigurationBuilder();
        builder.defaultCacheName(cacheName)
            .host(networkAddress)
            .authentication().disable()
            .topologyLockTimeout(topologyLockTimeout)
            .topologyReplTimeout(topologyReplTimeout);

        return builder.build();
    }

}
