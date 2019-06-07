# LengthFieldBasedFrameDecoder内容

```
childHandler(new ChannelInitializer<SocketChannel>() {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
          .addLast("encoder", new MessageEncoder())
          /**
            // HEADER_SIZE = version+type+requestId+isZip=int+byte+int+byte+int=4+1+4+1+4=14
            public static final int HEADER_SIZE = 14;
            private int version;    // 版本
            private byte type;      // 消息类型  0xAF表示心跳包, 0xBF表示超时包, 0xCF业务信息包, 订下下的规矩是: 心跳包的内容长度为0
            private int requestId;  // 请求id
            private byte isZip;     // 是否压缩, 0表示不压缩, 1表示压缩
            private int length;
            private String body;
          */
          /**
           * 第1个参数 maxFrameLength      解码时，处理每个帧数据的最大长度
           * 第2个参数 lengthFieldOffset   该帧数据中，存放该帧数据的长度的数据的起始位置
           * 第3个参数 lengthFieldLength   记录该帧数据长度的字段本身的长度
          */
          .addLast("decoder", new MessageDecoder(Constants.MAX_MESSAGE_LENGTH, 14, 4))
          .addLast(new ServerHandler());
    }
});
```

```
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    /***
     * LengthFieldBasedFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
     *                              int lengthAdjustment, int initialBytesToStrip, boolean failFast)
     * 参数 maxFrameLength      解码时，处理每个帧数据的最大长度
     * 参数 lengthFieldOffset   该帧数据中，存放该帧数据的长度的数据的起始位置
     * 参数 lengthFieldLength   记录该帧数据长度的字段本身的长度
     * 参数 lengthAdjustment    修改帧数据长度字段中定义的值，可以为负数
     * 参数 initialBytesToStrip 解析的时候需要跳过的字节数
     * 参数 failFast            为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异常
     * 参数 failFast            If true, a TooLongFrameException is thrown as soon as the decoder notices the length of the frame will exceed maxFrameLength
     *            regardless of whether the entire frame has been read. If false, a TooLongFrameExceptionis thrown after the entire frame
     *            that exceeds maxFrameLength has been read
     *
     * LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength) 底层会调用
     * LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip) lengthAdjustment和initialBytesToStrip都为零  底层调用
     * LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, true)
     * 即参数少的构造函数全部都通过默认参数来调用参数最全的构造函数
     * LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength)  等同于
     * LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0) 等同于
     * LengthFieldBasedFrameDecoder(maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0, true) 等同于
     * LengthFieldBasedFrameDecoder(ByteOrder.BIG_ENDIAN, maxFrameLength, lengthFieldOffset, lengthFieldLength, 0, 0, true)
     */
    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null)
            return null;

        if (in.readableBytes() <= MessageEntity.HEADER_SIZE)
            return null;

        in.markReaderIndex();

        int version = in.readInt();
        byte type = in.readByte();
        int requestId = in.readInt();
        byte isZip = in.readByte();
        int length = in.readInt();

        // FIXME 如果dataLength过大，可能导致问题
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return null;
        }

        if(isZip==1) {
            // 压缩解码逻辑。。。。。。。。
        }
        byte[] data = new byte[length];
        in.readBytes(data);

        String body = new String(data, Constants.CHARSET);
        MessageEntity msg = new MessageEntity(version, type, requestId, body);
        return msg;
    }
}
```