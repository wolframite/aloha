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
    private InetSocketAddress productInetSocketAddress;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.port.main}")
    private int mainPort;

    @Value("${memcached.port.product}")
    private int productPort;

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
    }

}
