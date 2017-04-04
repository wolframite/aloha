package com.zalora.aloha.storage;

import com.zalora.jmemcached.LocalCacheElement;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.metadata.Metadata;
import org.infinispan.server.memcached.MemcachedMetadata;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Hook up jMemcached and Infinispan
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
public class DefaultInfiniBridge extends AbstractInfiniBridge {

    private AdvancedCache<String, byte[]> ispanCache;

    public DefaultInfiniBridge(AdvancedCache<String, byte[]> ispanCache) {
        super(ispanCache);
        this.ispanCache = ispanCache;
    }

    @Override
    public LocalCacheElement get(Object key) {
        final String localKey = (String) key;
        CacheEntry<String, byte[]> ce = ispanCache.getCacheEntry(key);
        if (ce == null || ce.getValue() == null) {
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
    public LocalCacheElement putIfAbsent(String key, LocalCacheElement value) {
        byte[] prev = ispanCache.putIfAbsent(key, getDataFromCacheElement(value), generateMetadata(value));
        if (prev == null) {
            return null;
        }

        return value;
    }

    private LocalCacheElement generateLocalCacheItem(String key, CacheEntry<String, byte[]> cacheEntry) {
        MemcachedMetadata md = (MemcachedMetadata) cacheEntry.getMetadata();

        long expiration = md.lifespan() == -1 ? 0 : System.currentTimeMillis() + md.lifespan();
        LocalCacheElement item = new LocalCacheElement(key, getFlags(md), expiration, 0);
        item.setData(ChannelBuffers.copiedBuffer(cacheEntry.getValue()));

        return item;
    }

    private Metadata generateMetadata(Object localCacheElement) {
        LocalCacheElement lce = (LocalCacheElement) localCacheElement;

        MemcachedMetadata memcachedMetadata = new MemcachedMetadata(lce.getFlags(), generateVersion());

        long exp = lce.getExpire();
        if (exp > 0) {
            return memcachedMetadata.builder().lifespan(exp, TimeUnit.MILLISECONDS).build();
        }

        return memcachedMetadata;
    }

    private byte[] getDataFromCacheElement(LocalCacheElement localCacheElement) {
        byte[] data = new byte[localCacheElement.getData().capacity()];
        localCacheElement.getData().getBytes(0, data);
        return data;
    }

    /**
     * For now I'll just parse the flags from the toString() method, later I'll have to implement a
     * new Metadata class. The 33 btw. comes from the prefix in the toString() method, which doesn't change
     * @param memcachedMetadata
     * @return
     */
    private long getFlags(MemcachedMetadata memcachedMetadata) {
        String metaData = memcachedMetadata.toString();
        return Long.parseLong(metaData.substring(33, metaData.indexOf(',')));
    }

}
