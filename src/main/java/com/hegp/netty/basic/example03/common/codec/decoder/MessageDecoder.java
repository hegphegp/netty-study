package com.hegp.netty.basic.example03.common.codec.decoder;

import com.hegp.netty.basic.example03.common.domain.MessageEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    //头部信息的大小应该是 byte+byte+int = 1+1+8+4 = 14
    private static final int HEADER_SIZE = 14;

    /**
     public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
         this(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0);
     }

     public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
             int lengthAdjustment, int initialBytesToStrip) {
         this(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, true);
     }

     public LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
             int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
          this(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
     }

     public LengthFieldBasedFrameDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
             int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
         // 一些逻辑
     }
     */

    /**
     * @param maxFrameLength 解码时，处理每个帧数据的最大长度
     * @param lengthFieldOffset 该帧数据中，存放该帧数据的长度的数据的起始位置
     * @param lengthFieldLength 记录该帧数据长度的字段本身的长度
     * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数
     * @param initialBytesToStrip 解析的时候需要跳过的字节数
     * @param failFast 为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异常
     */
    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                         int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null)
            return null;

        if (in.readableBytes() <= HEADER_SIZE)
            return null;

        in.markReaderIndex();

        byte type = in.readByte();
        int requestId = in.readInt();
        int dataLength = in.readInt();

        // FIXME 如果dataLength过大，可能导致问题
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return null;
        }

        byte[] data = new byte[dataLength];
        in.readBytes(data);

        String body = new String(data, "UTF-8");
        MessageEntity msg = new MessageEntity(type, requestId, body);
        return msg;
    }
}