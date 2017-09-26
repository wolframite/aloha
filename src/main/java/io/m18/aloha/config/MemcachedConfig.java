package io.m18.aloha.config;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.server.memcached.configuration.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class MemcachedConfig {

    @Value("${infinispan.cache.name}")
    private String cacheName;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.port}")
    private int port;

    @Value("${memcached.idleTime}")
    private int idleTime;

    @Value("${memcached.recvBufSize}")
    private int recvBufSize;

    @Value("${memcached.sendBufSize}")
    private int sendBufSize;

    @Value("${memcached.tcpNoDelay}")
    private boolean tcpNoDelay;

    private MemcachedServerConfiguration memcachedServerConfiguration;

    @PostConstruct
    public void init() {
        MemcachedServerConfigurationBuilder builder = new MemcachedServerConfigurationBuilder();

        memcachedServerConfiguration = builder
            .name(cacheName)
            .host(host)
            .port(port)
            .idleTimeout(idleTime)
            .recvBufSize(recvBufSize)
            .sendBufSize(sendBufSize)
            .tcpNoDelay(tcpNoDelay)
            .build();
    }

    @Bean
    public MemcachedServerConfiguration memcachedServerConfiguration() {
        return memcachedServerConfiguration;
    }

}
