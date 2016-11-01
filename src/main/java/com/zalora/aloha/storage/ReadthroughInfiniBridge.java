package com.zalora.aloha.storage;

import com.zalora.aloha.models.entities.Item;
import com.zalora.jmemcached.LocalCacheElement;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.metadata.EmbeddedMetadata;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Hook up jMemcached and Infinispan
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
public class ReadthroughInfiniBridge extends AbstractInfiniBridge {

    private AdvancedCache<String, Item> ispanCache;

    public ReadthroughInfiniBridge(AdvancedCache<String, Item> ispanCache) {
        super(ispanCache);
        this.ispanCache = ispanCache;
    }

    @Override
    public LocalCacheElement get(Object key) {
        final String localKey = (String) key;
        CacheEntry<String, Item> ce = ispanCache.getCacheEntry(key);
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
        return null;
    }

    @Override
    public LocalCacheElement remove(Object key) {
        return null;
    }

    @Override
    public boolean remove(Object key, Object localCacheElement) {
        return false;
    }

    @Override
    public boolean replace(String key, LocalCacheElement searchElement, LocalCacheElement replaceElement) {
        return false;
    }

    @Override
    public LocalCacheElement replace(String key, LocalCacheElement localCacheElement) {
        return null;
    }

    @Override
    public LocalCacheElement putIfAbsent(String key, LocalCacheElement value) {
        return new LocalCacheElement(key);
    }

    private LocalCacheElement generateLocalCacheItem(String key, CacheEntry<String, Item> cacheEntry) {
        EmbeddedMetadata md = (EmbeddedMetadata) cacheEntry.getMetadata();
        long expiration = md.lifespan() == -1 ? 0 : System.currentTimeMillis() + md.lifespan();

        LocalCacheElement lce = new LocalCacheElement(key, cacheEntry.getValue().getFlags(), expiration, 0);
        lce.setData(ChannelBuffers.copiedBuffer(cacheEntry.getValue().getData()));

        return lce;
    }

}
