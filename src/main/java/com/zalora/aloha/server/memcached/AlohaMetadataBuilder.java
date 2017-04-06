package com.zalora.aloha.server.memcached;

import org.infinispan.metadata.Metadata;
import org.infinispan.metadata.EmbeddedMetadata.Builder;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public class AlohaMetadataBuilder extends Builder {

    private long flags;

    AlohaMetadataBuilder() {}

    AlohaMetadataBuilder flags(long flags) {
        this.flags = flags;
        return this;
    }

    public Metadata build() {
        return (Metadata) (this.hasLifespan() ?
            new AlohaExpirableMetadata(
                this.flags, this.version, this.lifespan.longValue(), this.lifespanUnit
            ) : new AlohaMetadata(this.flags, this.version));
    }
}
