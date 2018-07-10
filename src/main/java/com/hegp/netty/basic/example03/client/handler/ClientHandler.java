package com.hegp.netty.basic.example03.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端-服务端连接通道激活！"); //与服务端建立连接后
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String str = null;
        if (msg instanceof ByteBuf) {
            ByteBuf bb = (ByteBuf) msg;
            byte[] b = new byte[bb.readableBytes()];
            bb.readBytes(b);
            str = new String(b, "UTF-8");
        } else if (msg != null) {
            str = msg.toString();
        }
        System.out.println("客户端收到消息：" + str);
    }

    /** 当读取不到消息后时触发（注：会受到粘包、断包等影响，所以未必是客户定义的一个数据包读取完成即调用） */
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客服端读取服务端下发消息完毕！");
    }

    /** 异常捕捉方法 */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("客户端通道异常，异常消息：" + cause.getMessage());
        ctx.close();
    }

}