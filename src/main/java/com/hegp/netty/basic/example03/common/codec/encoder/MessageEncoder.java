package com.hegp.netty.basic.example03.common.codec.encoder;

import com.hegp.netty.basic.example03.common.constant.Constants;
import com.hegp.netty.basic.example03.common.domain.MessageEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<MessageEntity> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageEntity msg, ByteBuf out) throws Exception {
        /**
         private int version;    // 版本
         private byte type;      // 消息类型  0xAF表示心跳包, 0xBF表示超时包, 0xCF业务信息包, 订下下的规矩是: 心跳包的内容长度为0
         private int requestId;  // 请求id
         private byte isZip;     // 是否压缩, 0表示不压缩, 1表示压缩
         private int length;
         private String body;
         */
        out.writeInt(msg.getVersion());
        out.writeByte(msg.getType());
        out.writeInt(msg.getRequestId());
        out.writeByte(msg.getIsZip());
        out.writeInt(msg.getLength());
        byte[] data = msg.getBody().getBytes(Constants.CHARSET);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}