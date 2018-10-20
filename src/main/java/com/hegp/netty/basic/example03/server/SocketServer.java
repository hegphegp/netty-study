package com.hegp.netty.basic.example03.server;

import com.hegp.netty.basic.example03.common.codec.encoder.MessageEncoder;
import com.hegp.netty.basic.example03.common.constant.Constants;
import com.hegp.netty.basic.example03.common.domain.MessageEntity;
import com.hegp.netty.basic.example03.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                            // 继承LengthFieldBasedFrameDecoder是错误的，除非自认为自己继承的类比官方的还要厉害
                            /**
                             * public class MessageEntity {
                             *     // HEADER_SIZE = version + type + requestId + isZip = int + byte + int + byte + int = 4 + 1 + 4 + 1 + 4 = 14
                             *     public static final int HEADER_SIZE = 14;
                             *
                             *     // LENGTH_FIELD_LENGTH = length = int = 4
                             *     public static final int LENGTH_FIELD_LENGTH=4;
                             *
                             *     private int version;    // 版本
                             *     private byte type;      // 消息类型  0xAF表示心跳包, 0xBF表示超时包, 0xCF业务信息包, 订下下的规矩是: 心跳包的内容长度为0
                             *     private int requestId;  // 请求id
                             *     private byte isZip;     // 是否压缩, 0表示不压缩, 1表示压缩
                             *
                             *     private int length;
                             *     private String body;
                             */
                          .addLast("decoder", new LengthFieldBasedFrameDecoder(Constants.MAX_MESSAGE_LENGTH, MessageEntity.HEADER_SIZE, MessageEntity.LENGTH_FIELD_LENGTH))
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