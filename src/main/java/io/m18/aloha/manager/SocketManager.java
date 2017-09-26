package io.m18.aloha.manager;

import io.m18.aloha.config.SocketConfig;
import io.m18.aloha.socket.SocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class SocketManager {

    @Autowired
    private SocketConfig socketConfig;

    @Autowired
    private SocketServer socketServer;

    @PostConstruct
    public void init() {
        if (!socketConfig.isSocketEnabled()) {
            log.info("Socket is disabled");
            return;
        }

        try {
            socketServer.run();
            log.info("Socket is listening on {}", socketConfig.getSocketPath());
        } catch (Exception ex) {
            log.error("Failed to start socket server", ex);
        }
    }

}
