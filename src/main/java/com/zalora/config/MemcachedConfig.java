package com.zalora.config;

import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;
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
    private InetSocketAddress inetSocketAddress;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.port}")
    private int port;

    @Getter
    @Value("${memcached.idleTime}")
    private int idleTime;

    @Getter
    @Value("${memcached.verbose}")
    private boolean verbose;

    @Getter
    private CacheStorage<Key, LocalCacheElement> storage;

    @PostConstruct
    public void init() {
        inetSocketAddress = new InetSocketAddress(host, port);
        storage = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO, 1000000, 1073741824);
    }
}
