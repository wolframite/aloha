package io.m18.aloha.manager;

import lombok.extern.slf4j.Slf4j;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.server.hotrod.HotRodServer;
import org.infinispan.server.hotrod.configuration.HotRodServerConfiguration;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class HotRodManager {

    @Value("${hotrod.enabled}")
    private boolean enabled;

    @Autowired
    private HotRodServerConfiguration hotRodServerConfiguration;

    @Autowired
    private EmbeddedCacheManager embeddedCacheManager;

    private HotRodServer hotRodServer = new HotRodServer();

    @PostConstruct
    public void init() {
        if (!enabled) {
            log.info("HotRod is disabled");
            return;
        }

        hotRodServer.start(hotRodServerConfiguration, embeddedCacheManager);
        log.info("HotRod is running on {}:11222", hotRodServerConfiguration.host());
    }

}
