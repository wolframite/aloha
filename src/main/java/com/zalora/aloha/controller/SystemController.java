package com.zalora.aloha.controller;

import com.zalora.aloha.config.CacheConfig;
import com.zalora.aloha.manager.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@RestController
public class SystemController {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CacheConfig cacheConfig;

    @RequestMapping("/exit")
    public void gracefulShutdown() {
        if (cacheConfig.isPrimaryCacheEnabled()) {
            cacheManager.getPrimaryCache().stop();
        }

        if (cacheConfig.isSecondaryCacheEnabled()) {
            cacheManager.getSecondaryCache().stop();
        }

        cacheManager.getEmbeddedCacheManager().stop();
    }

}
