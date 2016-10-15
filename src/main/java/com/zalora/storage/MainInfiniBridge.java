package com.zalora.storage;

import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;

import com.zalora.manager.CacheManager;
import org.springframework.util.Assert;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class MainInfiniBridge extends AbstractInfiniBridge {

    @Autowired
    public MainInfiniBridge(CacheManager cacheManager) {
        Assert.notNull(cacheManager.getMainStorage(), "Infinispan Cache could not be loaded");
        this.ispanCache = cacheManager.getMainStorage();
    }

    @Override
    public LocalCacheElement put(Key key, LocalCacheElement value) {
        byte[] memcachedKey = getKeyAsByteArray(key);
        if (memcachedKey == null || memcachedKey.length == 0) {
            return null;
        }

        ispanCache.putAsync(memcachedKey, getDataAsByteArray(value));
        return value;
    }

}
