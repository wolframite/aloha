package com.zalora.aloha.config;

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
    private InetSocketAddress primaryInetSocketAddress;

    @Getter
    private InetSocketAddress secondaryInetSocketAddress;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.port.primary}")
    private int primaryPort;

    @Value("${memcached.port.secondary}")
    private int secondaryPort;

    @Getter
    @Value("${memcached.idleTime}")
    private int idleTime;

    @Getter
    @Value("${memcached.verbose}")
    private boolean verbose;

    @PostConstruct
    public void init() {
        primaryInetSocketAddress = new InetSocketAddress(host, primaryPort);
        secondaryInetSocketAddress = new InetSocketAddress(host, secondaryPort);
    }

}
