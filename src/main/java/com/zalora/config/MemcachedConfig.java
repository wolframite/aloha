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
    private InetSocketAddress mainInetSocketAddress;

    @Getter
    private InetSocketAddress sessionInetSocketAddress;

    @Getter
    private InetSocketAddress productInetSocketAddress;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.main.port}")
    private int mainPort;

    @Value("${memcached.product.port}")
    private int productPort;

    @Value("${memcached.session.port}")
    private int sessionPort;

    @Getter
    @Value("${memcached.idleTime}")
    private int idleTime;

    @Getter
    @Value("${memcached.verbose}")
    private boolean verbose;

    @PostConstruct
    public void init() {
        mainInetSocketAddress = new InetSocketAddress(host, mainPort);
        productInetSocketAddress = new InetSocketAddress(host, productPort);
        sessionInetSocketAddress = new InetSocketAddress(host, sessionPort);
    }

}
