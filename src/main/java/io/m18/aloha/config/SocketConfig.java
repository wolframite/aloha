package io.m18.aloha.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SocketConfig {

    @Getter
    @Value("${socket.path}")
    private String socketPath;

    @Getter
    @Value("${socket.enabled}")
    private boolean socketEnabled;

}
