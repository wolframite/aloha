package com.zalora.aloha.storage;

import com.zalora.jmemcached.LocalCacheElement;
import com.zalora.jmemcached.storage.CacheStorage;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.container.versioning.NumericVersionGenerator;
import org.infinispan.container.versioning.VersionGenerator;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.remoting.rpc.RpcManager;
import org.springframework.util.Assert;

/**
 * Hook up jMemcached and Infinispan
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
public abstract class AbstractInfiniBridge implements CacheStorage<String, LocalCacheElement> {

    private AdvancedCache<String, ?> ispanCache;

    AbstractInfiniBridge(AdvancedCache<String, ?> ispanCache) {
        Assert.notNull(ispanCache, "Infinispan Cache must not be null");
        this.ispanCache = ispanCache;
    }

    @Override
    public long getMemoryCapacity() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Number is wrong, but better than returning 0
     */
    @Override
    public long getMemoryUsed() {
        return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().freeMemory();
    }

    /**
     * Infinispan doesn't provide it, so let's make it up
     */
    @Override
    public int capacity() {
        return Math.round(ispanCache.size() * 1.1f);
    }

    @Override
    public void close() throws IOException {
        ispanCache.stop();
    }

    @Override
    public int size() {
        return ispanCache.size();
    }

    @Override
    public void clear() {
        ispanCache.clear();
    }

    @Override
    public boolean isEmpty() {
        return ispanCache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return ispanCache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return ispanCache.containsValue(value);
    }

    @Override
    public LocalCacheElement remove(Object key) {
        String localKey = (String) key;
        Object result = ispanCache.remove(localKey);
        if (result == null) {
            return null;
        }

        return new LocalCacheElement(localKey);
    }

    @Override
    public void putAll(Map<? extends String, ? extends LocalCacheElement> map) {
    }

    @Override
    public Set<String> keySet() {
        return null;
    }

    @Override
    public Collection<LocalCacheElement> values() {
        return null;
    }

    @Override
    public Set<Entry<String, LocalCacheElement>> entrySet() {
        return null;
    }

    protected EntryVersion generateVersion() {
        ComponentRegistry registry = ispanCache.getComponentRegistry();
        VersionGenerator cacheVersionGenerator = registry.getComponent(VersionGenerator.class);
        if (cacheVersionGenerator == null) {
            NumericVersionGenerator newVersionGenerator = new NumericVersionGenerator()
                .clustered(registry.getComponent(RpcManager.class) != null);
            registry.registerComponent(newVersionGenerator, VersionGenerator.class);
            return newVersionGenerator.nonExistingVersion();
        } else {
            return cacheVersionGenerator.nonExistingVersion();
        }
    }

}
