package com.hegp.netty.basic.example03.server;

import com.hegp.netty.basic.example03.common.encoder.MessageEncoder;
import com.hegp.netty.basic.example03.server.handler.ServerBusinessHandler;
import com.hegp.netty.basic.example03.common.encoder.MessageDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.net.InetSocketAddress;

/**
 * http://blog.csdn.net/linuu/article/details/51371595
 * http://blog.163.com/linfenliang@126/blog/static/127857195201210821145721/
 *
 * 重连
 * http://blog.csdn.net/z69183787/article/details/52625095
 * http://blog.csdn.net/chdhust/article/details/51649184
 */
public class ServerApp {

    private int port;

    public ServerApp(int port) {
        this.port = port;
    }

    public void start() {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            ServerBootstrap sbs = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                          .addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 10, 2, 4, 0, 0, true))
                          .addLast(new MessageDecoder())
                          .addLast(new ServerBusinessHandler())
                          .addLast(new MessageEncoder());

                    }
                }).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("绑定端口,开始接收TCP连接");
            ChannelFuture future = sbs.bind(port).sync();

            System.out.println("服务监听端口:" + port);
            future.channel().closeFuture().sync();

            System.out.println("Server Exit");

        } catch (Exception e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new ServerApp(8089).start();
    }
}
