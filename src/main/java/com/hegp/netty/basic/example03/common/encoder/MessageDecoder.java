package com.hegp.netty.basic.example03.common.encoder;

import com.hegp.netty.basic.example03.common.entity.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class MessageDecoder extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte type = buf.readByte();
        byte flag = buf.readByte();
        int length = buf.readInt();
        int len = buf.readableBytes();
        byte[] req = new byte[len];
        buf.readBytes(req);
        ctx.fireChannelRead(new Message(type, flag, req));
    }

}
