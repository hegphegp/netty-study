import com.hegp.netty.basic.example03.common.domain.MessageEntity;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null || in.readableBytes() <= MessageEntity.HEADER_SIZE)
            return null;

        in.markReaderIndex();
        byte type = in.readByte();
        int requestId = in.readInt();
        byte isZip = in.readByte();
        int length = in.readInt();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return null;
        }

        byte[] data = new byte[length];
        in.readBytes(data);
        String body = new String(data, "UTF-8");
        MessageEntity msg = new MessageEntity(type, requestId, body);
        return msg;
    }
}

public class SocketServer {

    protected static Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    private int port;

    public SocketServer(int port) {
        this.port = port;
    }

    public void startup() throws InterruptedException {
        try {
            workerGroup = new NioEventLoopGroup();
            bossGroup = new NioEventLoopGroup();
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .localAddress(port)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                .addLast("encoder", new MessageEncoder())
                                .addLast("decoder", new MessageDecoder(1<<20, 10, 4))
                        }
                    });
            channelFuture = bootstrap.bind().sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

public class MessageEntity {
    private final Charset charset = Charset.forName("utf-8");
    // 信息头部长度，byte + int + byte + int = 1 + 4 + 1 + 4 = 10
    public static final int HEADER_SIZE = 10;

    private byte type;      // 消息类型  0xAF表示心跳包, 0xBF表示超时包, 0xCF业务信息包, 订下下的规矩是: 心跳包的内容长度为0
    private int requestId;  // 请求id
    private byte isZip;     // 是否压缩
    private int length;
    private String body;
}