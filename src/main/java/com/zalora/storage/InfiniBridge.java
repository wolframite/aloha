package com.zalora.storage;

import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;
import org.infinispan.AdvancedCache;
import com.thimbleware.jmemcached.Key;
import com.zalora.manager.CacheManager;
import org.springframework.util.Assert;
import org.jboss.netty.buffer.ChannelBuffers;
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

    private final AdvancedCache<byte[], MemcachedItem> infinispanCache;

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
        return infinispanCache.containsKey(getKeyAsByteArray(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return infinispanCache.containsValue(new MemcachedItem(value));
    }

    @Override
    public LocalCacheElement get(Object key) {
        byte[] memcachedKey = getKeyAsByteArray(key);
        if (memcachedKey == null || memcachedKey.length == 0) {
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
        byte[] memcachedKey = getKeyAsByteArray(key);
        if (memcachedKey == null || memcachedKey.length == 0) {
            return null;
        }

        infinispanCache.put(memcachedKey, new MemcachedItem(localCacheElement));
        return localCacheElement;
    }

    @Override
    public LocalCacheElement remove(Object key) {
        infinispanCache.remove(getKeyAsByteArray(key));
        return new LocalCacheElement((Key) key);
    }

    @Override
    public void putAll(Map<? extends Key, ? extends LocalCacheElement> map) {
        Map<byte[], MemcachedItem> memcachedMap = new HashMap<>();
        for (Entry<? extends Key, ? extends LocalCacheElement> entry : map.entrySet()) {
            memcachedMap.put(
                entry.getKey().bytes.array(),
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
        return infinispanCache.keySet()
            .stream()
            .map(key -> new Key(ChannelBuffers.copiedBuffer(key)))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<LocalCacheElement> values() {
        return infinispanCache
            .values()
            .stream()
            .map(MemcachedItem::toLocalCacheElement)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<Key, LocalCacheElement>> entrySet() {
        return infinispanCache
            .entrySet()
            .stream()
            .map(entry -> new HashMap.SimpleEntry<>(
                new Key(ChannelBuffers.copiedBuffer(entry.getKey())),
                entry.getValue().toLocalCacheElement()
            )).collect(Collectors.toSet());
    }

    /**
     * Successful put operation has to return null here, because the JMemcached expects it
     * https://github.com/callan/jmemcached/blob/44c9c9c0b054abacf5347b0fd8bdf7dfefe3987e/src/com/thimbleware/jmemcached/CacheImpl.java#L83
     * @return null
     */
    @Override
    public LocalCacheElement putIfAbsent(Key key, LocalCacheElement localCacheElement) {
        infinispanCache.putIfAbsent(
            getKeyAsByteArray(key),
            new MemcachedItem(localCacheElement)
        );

        return null;
    }

    @Override
    public boolean remove(Object key, Object localCacheElement) {
        return infinispanCache.remove(getKeyAsByteArray(key), new MemcachedItem(localCacheElement));
    }

    @Override
    public boolean replace(Key key, LocalCacheElement localCacheElement, LocalCacheElement v1) {
        return infinispanCache.replace(
            getKeyAsByteArray(key),
            new MemcachedItem(localCacheElement),
            new MemcachedItem(v1)
        );
    }

    @Override
    public LocalCacheElement replace(Key key, LocalCacheElement localCacheElement) {
        infinispanCache.replace(
            getKeyAsByteArray(key),
            new MemcachedItem(localCacheElement)
        );

        return localCacheElement;
    }

    private byte[] getKeyAsByteArray(Object key) {
        return ((Key) key).bytes.array();
    }
}
