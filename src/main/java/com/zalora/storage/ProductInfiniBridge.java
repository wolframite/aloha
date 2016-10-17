package com.zalora.storage;

import com.thimbleware.jmemcached.Key;
import com.zalora.manager.CacheManager;
import org.springframework.util.Assert;
import org.springframework.stereotype.Component;
import com.thimbleware.jmemcached.LocalCacheElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
public class ProductInfiniBridge extends AbstractInfiniBridge {

    @Autowired
    public ProductInfiniBridge(CacheManager cacheManager) {
        Assert.notNull(cacheManager.getProductStorage(), "Product-Infinispan Cache could not be loaded");
        this.ispanCache = cacheManager.getProductStorage();
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
