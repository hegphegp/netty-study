package com.hegp.netty.basic.example03.server;

import com.hegp.netty.basic.example03.common.decoder.MessageDecoder;
import com.hegp.netty.basic.example03.common.encoder.MessageEncoder;
import com.hegp.netty.basic.example03.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SocketServer {

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private int port;

    public SocketServer(int port) {
        this.port = port;
    }

    public void startup() throws InterruptedException {
        try {
            workerGroup = new NioEventLoopGroup();
            bossGroup = new NioEventLoopGroup();
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
//                    .option(ChannelOption.SO_SNDBUF, 32 * 1024) // 设置发送缓冲大小
//                    .option(ChannelOption.SO_RCVBUF, 32 * 1024) // 这是接收缓冲大小
                    .localAddress(port) //server监听端口
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                .addLast("encoder", new MessageEncoder())
                                .addLast("decoder", new MessageDecoder(1<<20, 10, 4))
                                .addLast(new ServerHandler());
                        }
                    });
            channelFuture = bootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally { // 资源优雅释放
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}