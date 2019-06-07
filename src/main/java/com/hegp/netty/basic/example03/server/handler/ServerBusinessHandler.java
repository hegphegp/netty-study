package com.hegp.netty.basic.example03.server.handler;

import com.hegp.netty.basic.example03.common.entity.CustomMsg;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 1.SimpleChatServerHandler 继承自 SimpleChannelInboundHandler，这个类实现了ChannelInboundHandler接口，ChannelInboundHandler 提供了许多事件处理的接口方法，然后你可以覆盖这些方法。现在仅仅只需要继承 SimpleChannelInboundHandler 类而不是你自己去实现接口方法。
 2.覆盖了 handlerAdded() 事件处理方法。每当从服务端收到新的客户端连接时，客户端的 Channel 存入ChannelGroup列表中，并通知列表中的其他客户端 Channel
 3.覆盖了 handlerRemoved() 事件处理方法。每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
 4.覆盖了 channelRead0() 事件处理方法。每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。其中如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
 5.覆盖了 channelActive() 事件处理方法。服务端监听到客户端活动
 6.覆盖了 channelInactive() 事件处理方法。服务端监听到客户端不活动
 7.exceptionCaught() 事件处理方法是当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。然而这个方法的处理方式会在遇到不同异常的情况下有不同的实现，比如你可能想在关闭连接之前发送一个错误码的响应消息。


// ChannelGroup channels.writeAndFlush(msg);是群发消息的
public class ServerHandler extends SimpleChannelInboundHandler<MessageEntity> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 每当从服务端收到新的客户端连接时，客户端的 Channel 存入 ChannelGroup列表中，并通知列表中的其他客户端 Channel
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
//        Channel incoming = ctx.channel();
//        String body = "有新客户端上线"+incoming.remoteAddress() + " channel_id :" + incoming.id();
//        MessageEntity msg = new MessageEntity((byte) 0XAF, (byte) 0XBF, 1, body);
//        channels.writeAndFlush(msg);
        //添加到channelGroup 通道组
        channels.add(ctx.channel());
    }

    // 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，并通知列表中的其他客户端 Channel
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();
        String body = "有客户端下线"+incoming.remoteAddress() + " channel_id :" + incoming.id();
        int version = 1;
        MessageEntity msg = new MessageEntity(version, (byte) 0XBF, 1, body);
        channels.writeAndFlush(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
//        String body = "有新客户端上线"+incoming.remoteAddress() + " channel_id :" + incoming.id();
//        MessageEntity msg = new MessageEntity((byte) 0XAF, (byte) 0XBF, 1, body);
//        channels.writeAndFlush(msg);
        System.out.println("SimpleChatClient:"+incoming.remoteAddress()+"在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println("SimpleChatClient:"+incoming.remoteAddress()+"掉线");
    }

    // 每当从服务端读到客户端写入信息时，将信息转发给其他客户端的 Channel。其中如果你使用的是 Netty 5.x 版本时，需要把 channelRead0() 重命名为messageReceived()
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageEntity msg) throws Exception {
        System.out.println("服务器收到消息：" + msg.getBody());
        Channel incoming = ctx.channel();
        int version = 1;
        MessageEntity resp = new MessageEntity(version, msg.getType(), msg.getRequestId(), "接受其他客户端发来的信息");
        for (Channel channel : channels) {   //  遍历ChannelGroup中的channel
            if (channel != incoming) {       //  找到加入到ChannelGroup中的channel后，将录入的信息回写给除去发送信息的客户端
                channel.writeAndFlush(resp);
            }  else {
                MessageEntity msg1 = new MessageEntity(version, msg.getType(), msg.getRequestId(), "开始向其他客户端转发你的消息");
                channel.writeAndFlush(msg1);
            }
        }
    }

    // 当读取不到消息后时触发（注：会受到粘包、断包等影响，所以未必是客户定义的一个数据包读取完成即调用）
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务器读取服务端下发消息完毕！");
    }

    // 异常捕捉方法
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("服务器通道异常，异常消息：" + cause.getMessage());
        ctx.close();
    }
}
*/
public class ServerBusinessHandler extends SimpleChannelInboundHandler<CustomMsg> {

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /** 每当从服务端收到新的客户端连接时，客户端的 Channel 存入 ChannelGroup列表中，并通知列表中的其他客户端 Channel */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("有新客户端上线"+incoming.remoteAddress() + " channel_id :" + incoming.id());
//        MessageEntity msg = new MessageEntity((byte) 0XAF, (byte) 0XBF, 1, body);
//        channels.writeAndFlush(msg);
        //添加到channelGroup 通道组
        channels.add(ctx.channel());
    }

    /** 每当从服务端收到客户端断开时，客户端的 Channel 移除 ChannelGroup 列表中，并通知列表中的其他客户端 Channel */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();
        System.out.println("有客户端下线"+incoming.remoteAddress() + " channel_id :" + incoming.id());
        channels.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CustomMsg msg) throws Exception {
        System.out.println("服务器接收到远程客户端" + ctx.channel().remoteAddress() + "信息是" + msg.getBody());
//        System.out.println("进行业务处理");
//        msg.setBody("你发来的信息已处理");
//        处理完之后，给客户端发送信息
        ctx.channel().writeAndFlush(msg);
    }
}
