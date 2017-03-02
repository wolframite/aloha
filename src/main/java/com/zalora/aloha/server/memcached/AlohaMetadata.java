package com.zalora.aloha.server.memcached;

import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.server.memcached.MemcachedMetadata;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public class AlohaMetadata extends MemcachedMetadata {

    public AlohaMetadata(long flags, EntryVersion version) {
        super(flags, version);
    }

    public long flags() {
        return flags;
    }

}
