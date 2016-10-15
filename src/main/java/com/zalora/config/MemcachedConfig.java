package com.zalora.config;

import lombok.Getter;
import java.net.InetSocketAddress;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class MemcachedConfig {

    @Getter
    private InetSocketAddress storageInetSocketAddress;

    @Getter
    private InetSocketAddress sessionInetSocketAddress;

    @Getter
    private InetSocketAddress productInetSocketAddress;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.storage.port}")
    private int storagePort;

    @Value("${memcached.session.port}")
    private int sessionPort;

    @Value("${memcached.product.port}")
    private int productPort;

    @Getter
    @Value("${memcached.idleTime}")
    private int idleTime;

    @Getter
    @Value("${memcached.verbose}")
    private boolean verbose;

    @PostConstruct
    public void init() {
        storageInetSocketAddress = new InetSocketAddress(host, storagePort);
        sessionInetSocketAddress = new InetSocketAddress(host, sessionPort);
        productInetSocketAddress = new InetSocketAddress(host, productPort);
    }

}
