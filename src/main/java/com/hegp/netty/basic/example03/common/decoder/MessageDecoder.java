package com.hegp.netty.basic.example03.common.decoder;

import java.util.List;
 
import javax.xml.bind.DatatypeConverter;
 
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
//import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MessageDecoder extends MessageToMessageDecoder<ByteBuf> {
	
	private byte[] remainingBytes;
 
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		ByteBuf currBB = null;
		if(remainingBytes == null) {
			currBB = msg;
		}else {
			byte[] tb = new byte[remainingBytes.length + msg.readableBytes()];
			System.arraycopy(remainingBytes, 0, tb, 0, remainingBytes.length);
			byte[] vb = new byte[msg.readableBytes()];
			msg.readBytes(vb);
			System.arraycopy(vb, 0, tb, remainingBytes.length, vb.length);
			currBB = Unpooled.copiedBuffer(tb);
		}
		while(currBB.readableBytes() > 0) {
			if(!doDecode(ctx, currBB, out)) {
				break;
			}
		}
		if(currBB.readableBytes() > 0) {
			remainingBytes = new byte[currBB.readableBytes()];
			currBB.readBytes(remainingBytes);
		}else {
			remainingBytes = null;
		}
	}

	private boolean doDecode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
		if(msg.readableBytes() < 4)
			return false;
		msg.markReaderIndex();
		byte[] header = new byte[4];
		msg.readBytes(header);
		int len = Integer.parseInt(DatatypeConverter.printHexBinary(header), 16);
		if(msg.readableBytes() < len) {
			msg.resetReaderIndex();
			return false;
		}
		byte[] body = new byte[len];
		msg.readBytes(body);
		out.add(Unpooled.copiedBuffer(body));
		if(msg.readableBytes() > 0)
			return true;
		return false;
	}
	
}