package com.zalora.storage;

import java.nio.charset.Charset;
import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.jboss.netty.buffer.ChannelBuffers;
import org.springframework.stereotype.Component;

import org.infinispan.AdvancedCache;
import org.infinispan.metadata.Metadata;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.server.memcached.MemcachedMetadata;
import org.infinispan.container.versioning.VersionGenerator;
import org.infinispan.server.memcached.MemcachedMetadataBuilder;
import org.infinispan.container.versioning.NumericVersionGenerator;

import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.storage.CacheStorage;

import javax.transaction.*;

/**
 * Hook up jMemcached and Infinispan
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public abstract class AbstractInfiniBridge implements CacheStorage<Key, LocalCacheElement> {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    protected AdvancedCache<String, byte[]> ispanCache;

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
        return ispanCache.containsKey(getKeyAsString(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return ispanCache.containsValue(value);
    }

    @Override
    public LocalCacheElement get(Object memcKey) {
        final String key = getKeyAsString(memcKey);
        if (key == null || key.equals("")) {
            return null;
        }

        byte[] data = ispanCache.get(key);
        if (data == null) {
            return null;
        }

        return createLocalCacheItem(key, data, ispanCache.getCacheEntry(key).getMetadata());
    }

    @Override
    public LocalCacheElement remove(Object key) {
        ispanCache.remove(getKeyAsString(key));
        return new LocalCacheElement((Key) key);
    }

    @Override
    public void putAll(Map<? extends Key, ? extends LocalCacheElement> map) {}

    @Override
    public Set<Key> keySet() {
        return ispanCache.keySet()
            .stream()
            .map(key -> new Key(ChannelBuffers.copiedBuffer(key.getBytes(UTF8))))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<LocalCacheElement> values() {
        return null;
    }

    @Override
    public Set<Entry<Key, LocalCacheElement>> entrySet() {
        return null;
    }

    @Override
    public LocalCacheElement putIfAbsent(Key memcKey, LocalCacheElement value) {
        String key = getKeyAsString(memcKey);
        if (key == null || key.equals("")) {
            return null;
        }

        byte[] prev = ispanCache.get(key);
        if (prev == null) {
            Metadata metadata = createMetadata(value);
            ispanCache.putIfAbsent(key, getDataAsByteArray(value), metadata);
            return null;
        }

        value.setData(ChannelBuffers.copiedBuffer(prev));
        return value;

    }

    @Override
    public boolean remove(Object key, Object localCacheElement) {
        return ispanCache.remove(getKeyAsString(key), getDataAsByteArray(localCacheElement));
    }

    @Override
    public boolean replace(Key key, LocalCacheElement localCacheElement, LocalCacheElement v1) {
        return ispanCache.replace(
            getKeyAsString(key),
            getDataAsByteArray(localCacheElement),
            getDataAsByteArray(v1)
        );
    }

    @Override
    public LocalCacheElement replace(Key key, LocalCacheElement localCacheElement) {
        ispanCache.replace(
            getKeyAsString(key),
            getDataAsByteArray(localCacheElement)
        );

        return localCacheElement;
    }

    protected String getKeyAsString(Object key) {
        Key localKey = (Key) key;
        byte[] data = new byte[localKey.bytes.capacity()];
        localKey.bytes.readBytes(data);

        return new String(data);
    }

    protected byte[] getDataAsByteArray(Object value) {
        LocalCacheElement localCacheElement = (LocalCacheElement) value;
        byte[] data = new byte[localCacheElement.getData().capacity()];
        localCacheElement.getData().readBytes(data);

        return data;
    }

    protected LocalCacheElement createLocalCacheItem(String key, byte[] data, Metadata metadata) {
        MemcachedMetadata memcachedMetadata = (MemcachedMetadata) metadata;

        LocalCacheElement item = new LocalCacheElement(
            new Key(ChannelBuffers.copiedBuffer(key.getBytes(UTF8))),
            (int) memcachedMetadata.flags(),
            (int) memcachedMetadata.lifespan(),
            0
        );
        item.setData(ChannelBuffers.copiedBuffer(data));

        return item;
    }

    protected Metadata createMetadata(LocalCacheElement localCacheElement) {
        return new MemcachedMetadataBuilder()
            .flags(localCacheElement.getFlags())
            .version(generateVersion())
            .lifespan(localCacheElement.getExpire(), TimeUnit.SECONDS)
            .build();
    }

    /**
     * Taken from infinispan memcached implementation
     * https://github.com/infinispan/infinispan/blob/8.2.4.Final/server/memcached/src/main/scala/org/infinispan/server/memcached/MemcachedDecoder.scala#L388-L402
     */
    protected EntryVersion generateVersion() {
        ComponentRegistry registry = ispanCache.getComponentRegistry();
        VersionGenerator cacheVersionGenerator = registry.getComponent(VersionGenerator.class);
        if (cacheVersionGenerator == null) {
            // It could be null, for example when not running in compatibility mode.
            // The reason for that is that if no other component depends on the
            // version generator, the factory does not get invoked.
            NumericVersionGenerator newVersionGenerator = new NumericVersionGenerator()
                .clustered(registry.getComponent(RpcManager.class) != null);
            registry.registerComponent(newVersionGenerator, VersionGenerator.class);

            return newVersionGenerator.nonExistingVersion();
        } else {
            return cacheVersionGenerator.nonExistingVersion();
        }
    }
}
