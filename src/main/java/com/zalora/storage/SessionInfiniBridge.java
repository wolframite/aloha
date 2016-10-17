package com.zalora.storage;

import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;

import javax.transaction.*;
import lombok.extern.slf4j.Slf4j;
import com.zalora.manager.CacheManager;
import org.infinispan.metadata.EmbeddedMetadata;
import org.springframework.util.Assert;
import org.infinispan.metadata.Metadata;
import org.jboss.netty.buffer.ChannelBuffers;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class SessionInfiniBridge extends AbstractInfiniBridge {

    private static Metadata md;

    @Autowired
    public SessionInfiniBridge(CacheManager cacheManager) {
        Assert.notNull(cacheManager.getSessionStorage(), "Session Infinispan Cache could not be loaded");
        this.ispanCache = cacheManager.getSessionStorage();
    }

    @Override
    public LocalCacheElement put(Key memcKey, LocalCacheElement value) {
        String key = getKeyAsString(memcKey);
        if (key == null || key.equals("")) {
            return null;
        }

        ispanCache.put(key, getDataAsByteArray(value), createMetadata(value));
        return value;
    }

    @Override
    public LocalCacheElement putIfAbsent(Key memcKey, LocalCacheElement value) {
        String key = getKeyAsString(memcKey);
        if (key == null || key.equals("")) {
            return null;
        }

        byte[] prev = ispanCache.putIfAbsent(key, getDataAsByteArray(value), createMetadata(value));
        log.info("Prev: {}", prev);
        if (prev == null) {
            return null;
        }

        value.setData(ChannelBuffers.copiedBuffer(prev));
        return value;
    }

    public LocalCacheElement putIfAbsentTA(Key memcKey, LocalCacheElement value) {
        String key = getKeyAsString(memcKey);
        if (key == null || key.equals("")) {
            return null;
        }

        log.info("Cache Name: {}", ispanCache.getName());
        TransactionManager tm = ispanCache.getTransactionManager();

        try {
            tm.begin();

            byte[] prev = ispanCache.get(key);

            if (prev == null) {
                Metadata metadata = createMetadata(value);
                log.info("Meta Hash: {}", metadata.hashCode());

                ispanCache.putIfAbsent(key, getDataAsByteArray(value), metadata);

                tm.commit();
                return null;
            }

            tm.rollback();
            value.setData(ChannelBuffers.copiedBuffer(prev));
            return value;
        } catch (NotSupportedException |
                RollbackException |
                HeuristicMixedException |
                HeuristicRollbackException |
                SecurityException |
                IllegalStateException |
                SystemException ex) {

            log.error("TA failed", ex);

            try {
                tm.rollback();
            } catch (SystemException rex) {
                log.error("TA Rollback failed, that's fucked up", rex);
            }

            return value;
        }
    }

}
