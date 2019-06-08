package com.hegp.netty.basic.example03.common.encoder;

import com.hegp.netty.basic.example03.common.entity.Message;
import com.sun.istack.internal.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, @NotNull Message msg, ByteBuf out) throws Exception {
        byte[] body = msg.getBody();

        int length = body.length;
        if (length>1024) {
//            // 在此处统一压缩，选择那中压缩算法，选择哪种加密方式
//            byte[] newBody = body;
//            length = newBody.length;
        }
//        byte[] bodyBytes = body.getBytes(Charset.forName(Encoding));
//
        // new LengthFieldBasedFrameDecoder(1024 * 1024 * 10, 2, 4, 0, 0, true) 一定要与编码器的顺序一致，否则有问题
        // new LengthFieldBasedFrameDecoder(1024 * 1024 * 10, 2, 4, 0, 0, true) 与 out.write 的顺序完全一致
        // NSG:|1|1|4|BODY|
        out.writeByte(msg.getType());      // 系统编号
        out.writeByte(msg.getFlag());      // 信息标志
        out.writeInt(length);              // 消息长度
        out.writeBytes(body);              // 消息正文
    }
}
