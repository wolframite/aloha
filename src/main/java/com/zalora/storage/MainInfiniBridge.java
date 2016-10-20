package com.zalora.storage;

import com.zalora.jmemcached.LocalCacheElement;
import com.zalora.jmemcached.storage.CacheStorage;
import com.zalora.manager.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.container.versioning.NumericVersionGenerator;
import org.infinispan.container.versioning.VersionGenerator;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.metadata.Metadata;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.server.memcached.MemcachedMetadata;
import org.infinispan.server.memcached.MemcachedMetadataBuilder;
import org.jboss.netty.buffer.ChannelBuffers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Hook up jMemcached and Infinispan
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class MainInfiniBridge implements CacheStorage<String, LocalCacheElement> {

    protected AdvancedCache<String, byte[]> ispanCache;

    @Autowired
    public MainInfiniBridge(CacheManager cacheManager) {
        Assert.notNull(cacheManager.getMainCache(), "Infinispan Cache could not be loaded");
        this.ispanCache = cacheManager.getMainCache();
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
    public LocalCacheElement get(Object key) {
        final String localKey = (String) key;
        CacheEntry<String, byte[]> ce = ispanCache.getCacheEntry(key);
        if (ce == null) {
            return null;
        }

        return generateLocalCacheItem(localKey, ce);
    }

    @Override
    public Collection<LocalCacheElement> getMulti(Set<String> set) {
        return ispanCache.getAllCacheEntries(set).entrySet().stream()
            .map(entry -> generateLocalCacheItem(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    @Override
    public LocalCacheElement put(String key, LocalCacheElement value) {
        ispanCache.put(key, getDataFromCacheElement(value), generateMetadata(value));
        return value;
    }

    @Override
    public LocalCacheElement remove(Object key) {
        String localKey = (String) key;
        byte[] result = ispanCache.remove(localKey);
        if (result == null) {
            return null;
        }

        return new LocalCacheElement(localKey);
    }

    @Override
    public boolean remove(Object key, Object localCacheElement) {
        return ispanCache.remove(key, getDataFromCacheElement((LocalCacheElement) localCacheElement));
    }

    @Override
    public boolean replace(String key, LocalCacheElement searchElement, LocalCacheElement replaceElement) {
        return ispanCache.replace(
            key,
            getDataFromCacheElement(searchElement),
            getDataFromCacheElement(replaceElement),
            generateMetadata(replaceElement)
        );
    }

    @Override
    public LocalCacheElement replace(String key, LocalCacheElement localCacheElement) {
        byte[] result = ispanCache.replace(
            key,
            getDataFromCacheElement(localCacheElement),
            generateMetadata(localCacheElement)
        );

        if (result == null) {
            return null;
        }

        return localCacheElement;
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

    @Override
    public LocalCacheElement putIfAbsent(String key, LocalCacheElement value) {
        byte[] prev = ispanCache.putIfAbsent(key, getDataFromCacheElement(value), generateMetadata(value));
        if (prev == null) {
            return null;
        }

        return value;
    }

    protected LocalCacheElement generateLocalCacheItem(String key, CacheEntry<String, byte[]> cacheEntry) {
        MemcachedMetadata md = (MemcachedMetadata) cacheEntry.getMetadata();

        LocalCacheElement item = new LocalCacheElement(
            key, (int) md.flags(), md.lifespan(), 0
        );
        item.setData(ChannelBuffers.copiedBuffer(cacheEntry.getValue()));

        return item;
    }

    protected Metadata generateMetadata(Object localCacheElement) {
        LocalCacheElement lce = (LocalCacheElement) localCacheElement;
        Metadata.Builder mmb = new MemcachedMetadataBuilder()
            .flags(lce.getFlags())
            .version(generateVersion());

        long exp = lce.getExpire();
        if (exp > 0) {
            mmb.lifespan(exp, TimeUnit.MILLISECONDS);
        }

        return mmb.build();
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

    protected byte[] getDataFromCacheElement(LocalCacheElement localCacheElement) {
        byte[] data = new byte[localCacheElement.getData().capacity()];
        localCacheElement.getData().getBytes(0, data);
        return data;
    }
}
