package io.m18.aloha.controller;

import io.m18.aloha.config.CacheConfig;
import io.m18.aloha.manager.MemcachedManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@RestController
public class SystemController {

    @Autowired
    private EmbeddedCacheManager embeddedCacheManager;

    @Autowired
    private MemcachedManager memcachedManager;

    @Autowired
    private CacheConfig cacheConfig;

    @Autowired
    private ApplicationContext context;

    @RequestMapping("/exit")
    public void gracefulShutdown() {
        for (String cacheName : embeddedCacheManager.getCacheNames()) {
            embeddedCacheManager.getCache(cacheName).stop();
        }

        if (memcachedManager.getMainMemcachedDaemon() != null) {
            memcachedManager.getMainMemcachedDaemon().stop();
        }

        if (memcachedManager.getSessionMemcachedDaemon() != null) {
            memcachedManager.getSessionMemcachedDaemon().stop();
        }

        embeddedCacheManager.stop();

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException var2) {
                Thread.currentThread().interrupt();
            }

            SpringApplication.exit(context);
        });
        thread.setContextClassLoader(this.getClass().getClassLoader());
        thread.start();
    }

}
