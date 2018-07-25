package com.hegp.netty.basic.example03.server;

import com.hegp.netty.basic.example03.common.codec.decoder.MessageDecoder;
import com.hegp.netty.basic.example03.common.codec.encoder.MessageEncoder;
import com.hegp.netty.basic.example03.common.constant.Constants;
import com.hegp.netty.basic.example03.common.domain.MessageEntity;
import com.hegp.netty.basic.example03.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class SocketServer {

    protected static Logger logger = LoggerFactory.getLogger(SocketServer.class);

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
//                .option(ChannelOption.SO_SNDBUF, 32 * 1024) // 设置发送缓冲大小
//                .option(ChannelOption.SO_RCVBUF, 32 * 1024) // 这是接收缓冲大小
                .localAddress(port) //server监听端口
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                          .addLast("encoder", new MessageEncoder())
                            // MessageDecoder继承了LengthFieldBasedFrameDecoder，父类除了maxFrameLength参数外，其他参数都是没意义的，因为乱填任意错误数字都没有抛错，所有数据包都可以正确读取
                            // .addLast("decoder", new MessageDecoder(1<<20, 299, 4)) //是没任何问题的
                          .addLast("decoder", new MessageDecoder(Constants.MAX_MESSAGE_LENGTH, MessageEntity.HEADER_SIZE, 4))
                          .addLast(new ServerHandler());
                    }
                });
            channelFuture = bootstrap.bind().sync();
            channelFuture.channel().closeFuture()
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            logger.error("server closed");
                            // 服务器启动失败的一些逻辑处理
                        }
                    })
                    .sync();
        } finally { // 资源优雅释放
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}