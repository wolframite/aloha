package com.zalora.aloha.storage;

import com.zalora.aloha.compressor.Compressor;
import com.zalora.aloha.memcached.MemcachedItem;
import com.zalora.jmemcached.LocalCacheElement;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.AdvancedCache;
import org.jboss.netty.buffer.ChannelBuffers;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Hook up jMemcached and Infinispan
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
public class DefaultInfiniBridge extends AbstractInfiniBridge {

    private AdvancedCache<String, MemcachedItem> cache;
    private Compressor compressor;

    public DefaultInfiniBridge(AdvancedCache<String, MemcachedItem> cache, Compressor compressor) {
        super(cache);

        this.cache = cache;
        this.compressor = compressor;
    }

    @Override
    public LocalCacheElement get(Object key) {
        MemcachedItem item = cache.get(key);
        if (item == null) {
            return null;
        }

        compressor.afterGet(item);
        return createLocalCacheElement(item);
    }

    @Override
    public Collection<LocalCacheElement> getMulti(Set<String> keys) {
        return cache.getAll(keys).entrySet().stream()
            .map(entry -> {
                compressor.afterGet(entry.getValue());
                return createLocalCacheElement(entry.getValue());
            }).collect(Collectors.toList());
    }

    @Override
    public LocalCacheElement put(String key, LocalCacheElement localCacheElement) {
        MemcachedItem memcachedItem = createMemcachedItem(localCacheElement);
        compressor.beforePut(memcachedItem);

        if (localCacheElement.getExpire() > 0) {
            cache.put(key, memcachedItem, localCacheElement.getExpire(), TimeUnit.MILLISECONDS);
            return null;
        }

        cache.put(key, memcachedItem);
        return null;
    }

    @Override
    public boolean remove(Object key, Object localCacheElement) {
        cache.removeAsync(key, createMemcachedItem((LocalCacheElement) localCacheElement));
        return true;
    }

    @Override
    public LocalCacheElement replace(String key, LocalCacheElement localCacheElement) {
        MemcachedItem memcachedItem = createMemcachedItem(localCacheElement);
        compressor.beforePut(memcachedItem);

        cache.replace(key, memcachedItem, localCacheElement.getExpire(), TimeUnit.MILLISECONDS);
        return null;
    }

    @Override
    public boolean touch(String key, long expire) {
        MemcachedItem item = cache.get(key);
        if (item == null) {
            return false;
        }

        item.setExpire(expire);
        cache.replace(key, item, expire, TimeUnit.MILLISECONDS);
        return true;
    }

    @Override
    public LocalCacheElement putIfAbsent(String key, LocalCacheElement localCacheElement) {
        MemcachedItem memcachedItem = createMemcachedItem(localCacheElement);
        compressor.beforePut(memcachedItem);

        if (localCacheElement.getExpire() > 0) {
            cache.putIfAbsent(key, memcachedItem, localCacheElement.getExpire(), TimeUnit.MILLISECONDS);
            return null;
        }

        cache.putIfAbsent(key, memcachedItem);
        return null;
    }

    private LocalCacheElement createLocalCacheElement(MemcachedItem memcachedItem) {
        LocalCacheElement localCacheElement = new LocalCacheElement(
            memcachedItem.getKey(), memcachedItem.getFlags(), memcachedItem.getExpire(), 0
        );
        localCacheElement.setData(ChannelBuffers.copiedBuffer(memcachedItem.getData()));

        return localCacheElement;
    }

    private MemcachedItem createMemcachedItem(LocalCacheElement lce) {
        byte[] data = new byte[lce.getData().capacity()];
        lce.getData().getBytes(0, data);

        return new MemcachedItem(lce.getKey(), data, lce.getFlags(), lce.getExpire());
    }

}
