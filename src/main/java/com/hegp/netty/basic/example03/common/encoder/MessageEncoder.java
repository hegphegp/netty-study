package com.hegp.netty.basic.example03.common.encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import com.hegp.netty.basic.example03.common.domain.MessageEntity;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

public class MessageEncoder extends MessageToMessageEncoder<MessageEntity> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageEntity msg, List<Object> out) throws Exception {
        if (msg == null || msg.getHeader()==null)
            throw new Exception("The encode message is null");
        ByteBuf dataBuf = Unpooled.buffer();
        int dataSize = 0;
        byte[] data = null;
        if (msg.getBody() != null) {
            data = toByteArray(msg.getBody());
            dataSize = data.length;
        }
        dataBuf.writeInt(dataSize);
        if(dataSize>0)
            dataBuf.writeBytes(data);
        out.add(dataBuf);
    }

    public byte[] toByteArray (Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }

//	@Override
//	protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
//		if(msg == null)
//			return;
//		ByteBuf tb = null;
//		if(msg instanceof byte[]) {
//			tb = Unpooled.copiedBuffer((byte[])msg);
//		}else if(msg instanceof ByteBuf) {
//			tb = (ByteBuf) msg;
//		}else if(msg instanceof ByteBuffer) {
//			tb = Unpooled.copiedBuffer((ByteBuffer)msg);
//		}else {
//			String ostr = msg.toString();
//			tb = Unpooled.copiedBuffer(ostr, Charset.forName("UTF-8"));
//		}
//		byte[] pkg = new byte[4 + tb.readableBytes()];             //数据包
//		//
//		byte[] header = FormatUtils.intToBytes(tb.readableBytes());//报文包头
//		byte[] body = new byte[tb.readableBytes()];                //包体
//		tb.readBytes(body);
//		System.arraycopy(header, 0, pkg, 0, header.length);
//		System.arraycopy(body, 0, pkg, header.length, body.length);
//		out.add(Unpooled.copiedBuffer(pkg));
//	}

}