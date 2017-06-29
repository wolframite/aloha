package com.zalora.jmemcached;

import com.zalora.jmemcached.storage.CacheStorage;
import java.io.IOException;
import java.util.*;

/**
 * Default implementation of the cache handler, supporting local memory cache elements
 *
 * @author Ryan Daum
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public final class CacheImpl extends AbstractCache<LocalCacheElement> implements Cache<LocalCacheElement> {

    final CacheStorage<String, LocalCacheElement> storage;

    /**
     * @inheritDoc
     */
    public CacheImpl(CacheStorage<String, LocalCacheElement> storage) {
        super();
        this.storage = storage;
    }

    /**
     * Handle the deletion of an item from the cache.
     * Infinispan client doesn't return the object, but null to save network I/O
     * So we cannot determine if the delete actually worked and just hope for the best :-)
     *
     * @param key the key for the item
     * @return the message response
     */
    @Override
    public DeleteResponse delete(String key) {
        storage.remove(key);
        return DeleteResponse.DELETED;
    }

    /**
     * @inheritDoc
     */
    @Override
    public TouchResponse touch(String key, long expire) {
        if (storage.touch(key, expire)) {
            return TouchResponse.TOUCHED;
        }

        return TouchResponse.NOT_FOUND;
    }

    /**
     * @inheritDoc
     */
    @Override
    public StoreResponse add(LocalCacheElement e) {
        final long origCasUnique = e.getCasUnique();
        e.setCasUnique(casCounter.getAndIncrement());
        final boolean stored = storage.putIfAbsent(e.getKey(), e) == null;
        // we should restore the former cas so that the object isn't left dirty
        if (!stored) {
            e.setCasUnique(origCasUnique);
        }
        return stored ? StoreResponse.STORED : StoreResponse.NOT_STORED;
    }

    /**
     * @inheritDoc
     */
    @Override
    public StoreResponse replace(LocalCacheElement e) {
        return storage.replace(e.getKey(), e) != null ? StoreResponse.STORED : StoreResponse.NOT_STORED;
    }

    /**
     * @inheritDoc
     */
    @Override
    public StoreResponse append(LocalCacheElement element) {
        LocalCacheElement old = storage.get(element.getKey());
        if (old == null) {
            getMisses.incrementAndGet();
            return StoreResponse.NOT_FOUND;
        } else {
            return storage.replace(old.getKey(), old, old.append(element)) ? StoreResponse.STORED : StoreResponse.NOT_STORED;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public StoreResponse prepend(LocalCacheElement element) {
        LocalCacheElement old = storage.get(element.getKey());
        if (old == null) {
            getMisses.incrementAndGet();
            return StoreResponse.NOT_FOUND;
        } else {
            return storage.replace(old.getKey(), old, old.prepend(element)) ? StoreResponse.STORED : StoreResponse.NOT_STORED;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public StoreResponse set(LocalCacheElement e) {
        setCmds.incrementAndGet();//update stats
        e.setCasUnique(casCounter.getAndIncrement());
        storage.put(e.getKey(), e);

        return StoreResponse.STORED;
    }

    /**
     * @inheritDoc
     */
    @Override
    public StoreResponse cas(Long cas_key, LocalCacheElement e) {
        // have to get the element
        LocalCacheElement element = storage.get(e.getKey());
        if (element == null) {
            getMisses.incrementAndGet();
            return StoreResponse.NOT_FOUND;
        }

        if (element.getCasUnique() == cas_key) {
            // casUnique matches, now set the element
            e.setCasUnique(casCounter.getAndIncrement());
            if (storage.replace(e.getKey(), element, e)) return StoreResponse.STORED;
            else {
                getMisses.incrementAndGet();
                return StoreResponse.NOT_FOUND;
            }
        } else {
            // cas didn't match; someone else beat us to it
            return StoreResponse.EXISTS;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public Integer get_add(String key, int mod) {
        LocalCacheElement old = storage.get(key);
        if (old == null) {
            getMisses.incrementAndGet();
            return null;
        } else {
            LocalCacheElement.IncrDecrResult result = old.add(mod);
            return storage.replace(old.getKey(), old, result.replace) ? result.oldValue : null;
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public LocalCacheElement[] get(String... keys) {
        getCmds.incrementAndGet(); //updates stats

        LocalCacheElement[] elements = new LocalCacheElement[keys.length];
        int x = 0;
        int hits = 0;
        int misses = 0;

        if (keys.length > 1) {
            storage.getMulti(new HashSet<String>(Arrays.asList(keys))).toArray(elements);

            if (keys.length - elements.length > 0) {
                getMisses.addAndGet(keys.length - elements.length);
            }

            getHits.addAndGet(elements.length);
            return elements;
        }

        for (String key : keys) {
            LocalCacheElement e = storage.get(key);
            if (e == null) {
                misses++;
                elements[x] = null;
            } else {
                hits++;
                elements[x] = e;
            }

            x++;
        }

        getMisses.addAndGet(misses);
        getHits.addAndGet(hits);

        return elements;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean flush_all() {
        return flush_all(0);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean flush_all(int expire) {
        storage.clear();
        return true;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void close() throws IOException {
        storage.close();
    }

    /**
     * @inheritDoc
     */
    @Override
    protected Set<String> keys() {
        return storage.keySet();
    }

    /**
     * @inheritDoc
     */
    @Override
    public long getCurrentItems() {
        return storage.size();
    }

    /**
     * @inheritDoc
     */
    @Override
    public long getLimitMaxBytes() {
        return storage.getMemoryCapacity();
    }

    /**
     * @inheritDoc
     */
    @Override
    public long getCurrentBytes() {
        return storage.getMemoryUsed();
    }

}
