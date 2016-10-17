package com.zalora.storage;

import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;

import lombok.extern.slf4j.Slf4j;
import com.zalora.manager.CacheManager;
import org.springframework.util.Assert;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class MainInfiniBridge extends AbstractInfiniBridge {

    @Autowired
    public MainInfiniBridge(CacheManager cacheManager) {
        Assert.notNull(cacheManager.getMainStorage(), "Infinispan Cache could not be loaded");
        this.ispanCache = cacheManager.getMainStorage();
    }

    @Override
    public LocalCacheElement put(Key memcKey, LocalCacheElement value) {
        String key = getKeyAsString(memcKey);
        if (key == null || key.equals("")) {
            return null;
        }

        ispanCache.putAsync(key, getDataAsByteArray(value), createMetadata(value));
        return value;
    }

}
