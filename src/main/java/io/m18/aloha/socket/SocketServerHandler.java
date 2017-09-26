package io.m18.aloha.socket;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import java.util.*;
import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class SocketServerHandler extends ChannelInboundHandlerAdapter {

    private final String LINE_SEP = "\n";

    @Value("${infinispan.cache.name}")
    private String cacheName;

    @Autowired
    private EmbeddedCacheManager embeddedCacheManager;

    @Autowired
    private Cache<String, byte[]> cache;

    @PostConstruct
    public void init() {
        this.cache = embeddedCacheManager.getCache(cacheName);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String[] keys = msg.toString().split(" ");

        if (keys.length == 0) {
            ctx.writeAndFlush("null\n").addListener(ChannelFutureListener.CLOSE);
            return;
        }

        if (keys.length == 1) {
            byte[] item = cache.get(keys[0]);
            if (item == null) {
                ctx.writeAndFlush("").addListener(ChannelFutureListener.CLOSE);
                return;
            }

            ctx.writeAndFlush(new String(item) + "\n").addListener(ChannelFutureListener.CLOSE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        Map<String, byte[]> resultMap = cache.getAdvancedCache().getAll(new HashSet<>(Arrays.asList(keys)));

        if (resultMap == null || resultMap.size() == 0) {
            ctx.writeAndFlush("").addListener(ChannelFutureListener.CLOSE);
            return;
        }

        resultMap.forEach((key, value) -> sb.append(key).append(LINE_SEP).append(new String(value)).append(LINE_SEP));
        ctx.writeAndFlush(sb.toString()).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
        log.error("Socky made a mess", t);
        ctx.close();
    }

}
