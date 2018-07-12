package com.hegp.netty.basic.example03.common.encoder;

import com.hegp.netty.basic.example03.common.domain.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

public class MessageEncoder extends MessageToByteEncoder<Message> {
    private final Charset charset = Charset.forName("utf-8");

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        out.writeByte(msg.getMagicType());
        out.writeByte(msg.getType());
        out.writeLong(msg.getRequestId());
        byte[] data = msg.getBody().getBytes(charset);
        out.writeInt(data.length);
        out.writeBytes(data);

        /**
         private byte magicType;
         private byte type;//消息类型  0xAF 表示心跳包    0xBF 表示超时包  0xCF 业务信息包
         private long requestId; //请求id
         private int length;
         private String body;
         */
    }
}