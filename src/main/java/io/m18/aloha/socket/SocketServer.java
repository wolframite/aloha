package io.m18.aloha.socket;

import io.m18.aloha.config.SocketConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.logging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class SocketServer {

    private final String socketPath;
    private final SocketServerInitializer socketServerInitializer;

    @Autowired
    public SocketServer(SocketServerInitializer socketServerInitializer, SocketConfig socketConfig) {
        this.socketPath = socketConfig.getSocketPath();
        this.socketServerInitializer = socketServerInitializer;
    }

    @Async
    public void run() {
        EventLoopGroup masterGroup = new EpollEventLoopGroup();
        EventLoopGroup workerGroup = new EpollEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(masterGroup, workerGroup)
                .localAddress(new DomainSocketAddress(socketPath))
                .channel(EpollServerDomainSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(socketServerInitializer);

            bootstrap.bind().sync().channel().closeFuture().sync();
        } catch (InterruptedException ex) {
            log.info("Socket server error", ex);
        } finally {
            workerGroup.shutdownGracefully();
            masterGroup.shutdownGracefully();
        }
    }

}
