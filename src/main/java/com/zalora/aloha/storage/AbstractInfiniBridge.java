package com.zalora.aloha.storage;

import com.zalora.jmemcached.LocalCacheElement;
import com.zalora.jmemcached.storage.CacheStorage;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.springframework.util.Assert;
import java.io.IOException;
import java.util.*;

/**
 * Hook up jMemcached and Infinispan
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
public abstract class AbstractInfiniBridge implements CacheStorage<String, LocalCacheElement> {

    private AdvancedCache<String, ?> cache;

    AbstractInfiniBridge(AdvancedCache<String, ?> cache) {
        Assert.notNull(cache, "Infinispan Cache must not be null");
        this.cache = cache;
    }

    @Override
    public long getMemoryCapacity() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Return a rough estimate instead of zero
     */
    @Override
    public long getMemoryUsed() {
        return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
    }

    /**
     * Removed for performance reasons
     */
    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public void close() throws IOException {
        cache.stop();
    }

    /**
     * Removed for performance reasons
     */
    @Override
    public int size() {
        return cache.getStats().getCurrentNumberOfEntries();
    }

    @Override
    public void clear() {
        cache.clear();
        log.warn("Flushed {} cache", cache.getName());
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return cache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return cache.containsValue(value);
    }

    @Override
    public LocalCacheElement remove(Object key) {
        cache.removeAsync((String) key);
        return null;
    }

    /**
     * Only replace by key is in use
     */
    @Override
    public boolean replace(String key, LocalCacheElement searchElement, LocalCacheElement replaceElement) {
        return false;
    }

    // The memcached protocol does not support those operations, so they're not implemented here
    @Override
    public void putAll(Map<? extends String, ? extends LocalCacheElement> map) {
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>();
    }

    @Override
    public Collection<LocalCacheElement> values() {
        return new HashSet<>();
    }

    @Override
    public Set<Entry<String, LocalCacheElement>> entrySet() {
        return new HashSet<>();
    }

}
