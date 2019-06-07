package com.hegp.netty.basic.example03.client.handler;

import com.hegp.netty.basic.example03.common.entity.CustomMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
public class ClientHandler extends SimpleChannelInboundHandler<CustomMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MessageEntity msg) throws Exception {
        System.out.println("客户端收到消息：" + msg.getBody());
    }

    // 当读取不到消息后时触发（注：会受到粘包、断包等影响，所以未必是客户定义的一个数据包读取完成即调用）
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客服端读取服务端下发消息完毕！");
    }

    // 异常捕捉方法
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("客户端通道异常，异常消息：" + cause.getMessage());
        ctx.close();
    }
}
*/

public class ClientBusinessHandler extends SimpleChannelInboundHandler<CustomMsg> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CustomMsg customMsg) throws Exception {
        /** 接收到服务器的信息后，进行处理 */
//        System.out.println(String.format("ip:%s %s", channelHandlerContext.channel().remoteAddress(), customMsg));
        System.out.println("接收到服务器的信息是"+customMsg);
    }
}
