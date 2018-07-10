package com.hegp.netty.basic.example01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TimeServer {

    public void bind(int port) throws Exception {
        // 配置服务端的NIO线程组
        // NioEventLoopGroup是个线程组，它包含了一组NIO线程，专门用于网络事件的处理，实际上它们就是Reactor线程组。
        // 这里创建两个的原因是一个用于服务端接受客户端的连接，另一个用于进行SocketChannel的网络读写。
        // ServerBootstrap对象是Netty用于启动NIO服务端的辅助启动类，目的降低服务端的开发复杂度。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // netty服务端只提供ServerBootstrap一个Server的类
            ServerBootstrap b = new ServerBootstrap();
            // ServerBootstrap提供两个group方法 group(EventLoopGroup group) 和 group(EventLoopGroup parentGroup, EventLoopGroup childGroup) 方法
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                // ChannelInitializer<C extends Channel> 包含了大量的方法
                .childHandler(new ChildChannelHandler());   // 添加ChannelInitializer，该ChannelInitializer可以添加多个ChannelHandlerAdapter
            /* 直接用简写方法 b.bind("192.168.1.169", port).sync().channel().closeFuture().sync(); */
            b.bind(port).sync().channel().closeFuture().sync();
            /**
            ChannelFuture f = b.bind(host, port).sync(); // 绑定端口，同步等待成功
            f.channel().closeFuture().sync(); // 等待服务端监听端口关闭
            */
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) throws Exception {
        new TimeServer().bind(8080);
    }
}
