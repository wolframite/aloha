package com.zalora.storage;

import java.util.*;
import java.io.IOException;
import org.infinispan.Cache;
import com.thimbleware.jmemcached.Key;
import com.zalora.manager.CacheManager;
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

    private final Cache<Key, LocalCacheElement> infinispanCache;

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
        return infinispanCache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return infinispanCache.containsValue(value);
    }

    @Override
    public LocalCacheElement get(Object localCacheElement) {
        return infinispanCache.get(localCacheElement);
    }

    @Override
    public LocalCacheElement put(Key key, LocalCacheElement localCacheElement) {
        return infinispanCache.put(key, localCacheElement);
    }

    @Override
    public LocalCacheElement remove(Object localCacheElement) {
        return infinispanCache.remove(localCacheElement);
    }

    @Override
    public void putAll(Map<? extends Key, ? extends LocalCacheElement> map) {
        infinispanCache.putAll(map);
    }

    @Override
    public void clear() {
        infinispanCache.clear();
    }

    @Override
    public Set<Key> keySet() {
        return infinispanCache.keySet();
    }

    @Override
    public Collection<LocalCacheElement> values() {
        return infinispanCache.values();
    }

    @Override
    public Set<Entry<Key, LocalCacheElement>> entrySet() {
        return infinispanCache.entrySet();
    }

    @Override
    public LocalCacheElement putIfAbsent(Key key, LocalCacheElement localCacheElement) {
        return infinispanCache.putIfAbsent(key, localCacheElement);
    }

    @Override
    public boolean remove(Object o, Object o1) {
        return infinispanCache.remove(o, o1);
    }

    @Override
    public boolean replace(Key key, LocalCacheElement localCacheElement, LocalCacheElement v1) {
        return infinispanCache.replace(key, localCacheElement, v1);
    }

    @Override
    public LocalCacheElement replace(Key key, LocalCacheElement localCacheElement) {
        return infinispanCache.replace(key, localCacheElement);
    }
}
