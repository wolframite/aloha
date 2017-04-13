package com.zalora.aloha.storage;

import com.zalora.aloha.manager.CacheManager;
import com.zalora.aloha.manager.MemcachedManager;
import com.zalora.jmemcached.LocalCacheElement;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class DefaultInfiniBridgeTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MemcachedManager memcachedManager;

    private DefaultInfiniBridge defaultInfiniBridge;

    @PostConstruct
    public void init() {
        defaultInfiniBridge = new DefaultInfiniBridge(cacheManager.getPrimaryCache());

        LocalCacheElement lce1 = new LocalCacheElement("Hallo", 0, 0, 0);
        lce1.setData(ChannelBuffers.copiedBuffer("Test", Charset.defaultCharset()));

        defaultInfiniBridge.put("Hallo", lce1);
    }

    @Test
    public void testGet() {
        LocalCacheElement lce1 = defaultInfiniBridge.get("Hallo");
        assertThat(lce1.getExpire()).isEqualTo(0L);
    }
}
