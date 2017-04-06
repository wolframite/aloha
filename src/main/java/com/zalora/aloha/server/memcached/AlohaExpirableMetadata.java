package com.zalora.aloha.server.memcached;

import org.infinispan.container.versioning.EntryVersion;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public class AlohaExpirableMetadata extends AlohaMetadata {

    private final long lifespanTime;
    private final TimeUnit lifespanUnit;

    AlohaExpirableMetadata(long flags, EntryVersion version, long lifespanTime, TimeUnit lifespanUnit) {
        super(flags, version);
        this.lifespanTime = lifespanTime;
        this.lifespanUnit = Objects.requireNonNull(lifespanUnit);
    }

    public long lifespan() {
        return this.lifespanUnit.toMillis(this.lifespanTime);
    }

    public Builder builder() {
        return (new AlohaMetadataBuilder())
            .flags(this.flags)
            .version(this.version)
            .lifespan(this.lifespanTime, this.lifespanUnit);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            if (!super.equals(o)) {
                return false;
            } else {
                AlohaExpirableMetadata that = (AlohaExpirableMetadata) o;
                return this.lifespanTime == that.lifespanTime && this.lifespanUnit == that.lifespanUnit;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (this.lifespanTime ^ this.lifespanTime >>> 32);
        result = 31 * result + this.lifespanUnit.hashCode();
        return result;
    }

    public String toString() {
        return "AlohaExpirableMetadata{flags=" + this.flags + ", version=" +
            this.version + ", lifespanTime=" + this.lifespanTime + ", lifespanUnit=" + this.lifespanUnit + "} ";
    }

}
