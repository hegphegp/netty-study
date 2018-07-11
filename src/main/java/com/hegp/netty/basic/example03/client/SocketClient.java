package com.hegp.netty.basic.example03.client;

import java.util.HashMap;
import java.util.Map;

import com.hegp.netty.basic.example03.client.handler.ClientHandler;
import com.hegp.netty.basic.example03.common.decoder.MessageDecoder;
import com.hegp.netty.basic.example03.common.encoder.MessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SocketClient {

    //存放NIO客户端实例对象
    private static Map<String, SocketClient> map = new HashMap<String, SocketClient>();

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
                    .addLast("decoder", new MessageDecoder(1<<20, 10, 4))
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
        channelFuture.channel().writeAndFlush(obj);
    }

    public void close() throws InterruptedException {
        channelFuture.channel().closeFuture().sync();
        group.shutdownGracefully();
        map.remove(ip + ":" + serverPort);
    }

}