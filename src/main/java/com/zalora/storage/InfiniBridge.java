package com.zalora.storage;

import java.util.*;
import java.io.IOException;
import org.infinispan.Cache;
import java.nio.charset.Charset;
import com.thimbleware.jmemcached.Key;
import com.zalora.manager.CacheManager;
import org.jboss.netty.buffer.ChannelBuffers;
import org.springframework.util.Assert;
import org.springframework.stereotype.Component;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.storage.CacheStorage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Hook up jMemcached and Infinispan
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class InfiniBridge implements CacheStorage<Key, LocalCacheElement> {

    private final Cache<String, MemcachedItem> infinispanCache;
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Autowired
    public InfiniBridge(CacheManager cacheManager) {
        Assert.notNull(cacheManager.getMainStorage(), "Infinispan Cache could not be loaded");
        this.infinispanCache = cacheManager.getMainStorage();
    }

    @Override
    public long getMemoryCapacity() {
        return Runtime.getRuntime().maxMemory();
    }

    @Override
    public long getMemoryUsed() {
        return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public int capacity() {
        return infinispanCache.size();
    }

    @Override
    public void close() throws IOException {
        infinispanCache.stop();
    }

    @Override
    public int size() {
        return infinispanCache.size();
    }

    @Override
    public boolean isEmpty() {
        return infinispanCache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return infinispanCache.containsKey(getKeyAsString(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return infinispanCache.containsValue(new MemcachedItem((LocalCacheElement) value));
    }

    @Override
    public LocalCacheElement get(Object key) {
        String memcachedKey = getKeyAsString(key);
        if (memcachedKey == null || memcachedKey == "") {
            return null;
        }

        MemcachedItem memcachedItem = infinispanCache.get(memcachedKey);
        if (memcachedItem == null) {
            return null;
        }

        return memcachedItem.toLocalCacheElement();
    }

    @Override
    public LocalCacheElement put(Key key, LocalCacheElement localCacheElement) {
        String memcachedKey = getKeyAsString(key);
        if (memcachedKey == null || memcachedKey == "") {
            return null;
        }

        infinispanCache.put(memcachedKey, new MemcachedItem(localCacheElement));
        return localCacheElement;
    }

    @Override
    public LocalCacheElement remove(Object key) {
        infinispanCache.remove(getKeyAsString(key));
        return new LocalCacheElement((Key) key);
    }

    @Override
    public void putAll(Map<? extends Key, ? extends LocalCacheElement> map) {
        Map<String, MemcachedItem> memcachedMap = new HashMap<>();
        for (Entry<? extends Key, ? extends LocalCacheElement> entry : map.entrySet()) {
            memcachedMap.put(
                entry.getKey().bytes.toString(UTF_8),
                new MemcachedItem(entry.getValue())
            );
        }

        infinispanCache.putAll(memcachedMap);
    }

    @Override
    public void clear() {
        infinispanCache.clear();
    }

    @Override
    public Set<Key> keySet() {
        Set<Key> jMemcachedKeySet = new HashSet<>();

        for (String key : infinispanCache.keySet()) {
            jMemcachedKeySet.add(new Key(ChannelBuffers.copiedBuffer(key.getBytes(UTF_8))));
        }

        return jMemcachedKeySet;
    }

    @Override
    public Collection<LocalCacheElement> values() {
        return null;
    }

    @Override
    public Set<Entry<Key, LocalCacheElement>> entrySet() {
        return null;
    }

    @Override
    public LocalCacheElement putIfAbsent(Key key, LocalCacheElement localCacheElement) {
        MemcachedItem memcachedItem = infinispanCache.putIfAbsent(
            getKeyAsString(key),
            new MemcachedItem(localCacheElement)
        );
        return localCacheElement;
    }

    @Override
    public boolean remove(Object key, Object localCacheElement) {
        return infinispanCache.remove(getKeyAsString(key), new MemcachedItem(localCacheElement));
    }

    @Override
    public boolean replace(Key key, LocalCacheElement localCacheElement, LocalCacheElement v1) {
        return infinispanCache.replace(
            getKeyAsString(key),
            new MemcachedItem(localCacheElement),
            new MemcachedItem(v1)
        );
    }

    @Override
    public LocalCacheElement replace(Key key, LocalCacheElement localCacheElement) {
        infinispanCache.replace(
            getKeyAsString(key),
            new MemcachedItem(localCacheElement)
        );

        return localCacheElement;
    }

    private String getKeyAsString(Object key) {
        return ((Key) key).bytes.toString(UTF_8);
    }
}
