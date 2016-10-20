package com.zalora.config;

import java.net.InetSocketAddress;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class MemcachedConfig {

    @Getter
    private InetSocketAddress mainInetSocketAddress;

    @Getter
    private InetSocketAddress productInetSocketAddress;

    @Getter
    private InetSocketAddress sessionInetSocketAddress;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.port.main}")
    private int mainPort;

    @Value("${memcached.port.product}")
    private int productPort;

    @Value("${memcached.port.session}")
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
