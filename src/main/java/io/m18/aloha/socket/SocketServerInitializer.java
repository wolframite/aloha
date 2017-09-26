package io.m18.aloha.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.unix.DomainSocketChannel;
import io.netty.handler.codec.*;
import io.netty.handler.codec.string.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Slf4j
@Component
public class SocketServerInitializer extends ChannelInitializer<DomainSocketChannel> {

    @Autowired
    private SocketServerHandler socketServerHandler;

    @Override
    protected void initChannel(DomainSocketChannel domainSocketChannel) throws Exception {
        domainSocketChannel.pipeline()
            .addLast(new DelimiterBasedFrameDecoder(8192, true, Delimiters.lineDelimiter()))

            .addLast(new StringDecoder())
            .addLast(new StringEncoder())

            .addLast(socketServerHandler);
    }

}
