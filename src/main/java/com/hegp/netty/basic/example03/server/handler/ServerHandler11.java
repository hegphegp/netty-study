package com.hegp.netty.basic.example03.server.handler;

import java.nio.charset.Charset;
 
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler11 extends ChannelInboundHandlerAdapter {
	
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("服务端-客户端连接通道激活！");//与客户端建立连接后
    }
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		String str = null;
		if(msg instanceof ByteBuf) {
			ByteBuf bb = (ByteBuf)msg;
			str = bb.toString(Charset.forName("UTF-8"));
		}else if(msg != null){
			str = msg.toString();
		}
		System.out.println("服务端收到客户端消息：" + str);
	}
	
	/**当读取不到消息后时触发（注：会受到粘包、断包等影响，所以未必是客户定义的一个数据包读取完成即调用）*/
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("服务端读取客户端上送消息完毕！");
		ctx.channel().writeAndFlush(Unpooled.copiedBuffer("server has been received.", Charset.forName("UTF-8")));
	}
	
	/**异常捕捉方法 */
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println("服务端通道异常，异常消息："  + cause.getMessage());
		ctx.close();
	}
 
}