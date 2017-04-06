package com.zalora.aloha.server.memcached;

import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.metadata.Metadata;

import java.io.Serializable;

/**
 * Same as MemcachedMetadata, only my version exposes the flags
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public class AlohaMetadata implements Metadata, Serializable {

    protected final long flags;
    protected final EntryVersion version;

    public AlohaMetadata(long flags, EntryVersion version) {
        this.flags = flags;
        this.version = version;
    }

    public long flags() {
        return this.flags;
    }

    public long lifespan() {
        return -1L;
    }

    public long maxIdle() {
        return -1L;
    }

    public EntryVersion version() {
        return this.version;
    }

    public Builder builder() {
        return (new AlohaMetadataBuilder()).flags(this.flags).version(this.version);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            AlohaMetadata that = (AlohaMetadata) o;
            return this.flags == that.flags && this.version.equals(that.version);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = (int)(this.flags ^ this.flags >>> 32);
        result = 31 * result + this.version.hashCode();
        return result;
    }

    public String toString() {
        return "AlohaMetadata{flags=" + this.flags + ", version=" + this.version + '}';
    }

}
