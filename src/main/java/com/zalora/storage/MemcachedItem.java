package com.zalora.storage;

import lombok.Data;
import java.io.Serializable;
import java.nio.charset.Charset;
import com.thimbleware.jmemcached.Key;
import org.jboss.netty.buffer.ChannelBuffers;
import com.thimbleware.jmemcached.LocalCacheElement;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Data
public class MemcachedItem implements Serializable {

    public static transient Charset UTF_8 = Charset.forName("UTF-8");

    String key;
    byte[] value;
    int flags;
    int expire = 0;

    public MemcachedItem(Object localCacheElement) {
        LocalCacheElement item = (LocalCacheElement) localCacheElement;

        key = new String(item.getKey().bytes.array());
        value = item.getData().array();
        flags = item.getFlags();
        expire = item.getExpire();
    }

    public LocalCacheElement toLocalCacheElement() {
        Key key = new Key(ChannelBuffers.copiedBuffer(getKey().getBytes()));
        LocalCacheElement item = new LocalCacheElement(key, getFlags(), getExpire(), 0);
        item.setData(ChannelBuffers.copiedBuffer(getValue()));

        return item;
    }

}
