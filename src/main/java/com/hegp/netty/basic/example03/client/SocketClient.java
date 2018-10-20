package com.hegp.netty.basic.example03.client;

import com.hegp.netty.basic.example03.client.handler.ClientHandler;
import com.hegp.netty.basic.example03.common.codec.encoder.MessageEncoder;
import com.hegp.netty.basic.example03.common.constant.Constants;
import com.hegp.netty.basic.example03.common.domain.MessageEntity;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.HashMap;
import java.util.Map;

public class SocketClient {

    //存放NIO客户端实例对象
    private static Map<String, SocketClient> map = new HashMap();

    private String ip;
    private int serverPort;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    public ChannelFuture channelFuture;

    public SocketClient(String ip, int port) {
        this.ip = ip;
        this.serverPort = port;

    }

    public void startup() throws InterruptedException {
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
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
                    .addLast(new ClientHandler());
                }
            });
        channelFuture = bootstrap.connect(this.ip, serverPort).sync();
    }

    public void write(Object obj) {
        if (!channelFuture.channel().isOpen()
                || !channelFuture.channel().isWritable()
                || obj == null)
            return;
        System.out.println("client.writeAndFlush==>>>"+obj);
        channelFuture.channel().writeAndFlush(obj);
    }

    public void close() throws InterruptedException {
        channelFuture.channel().closeFuture().sync();
        group.shutdownGracefully();
        map.remove(ip + ":" + serverPort);
    }

}