package com.lfx.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/*
 *MessageToByteEncoder:nettyµÄ±àÂëÆ÷
 */
public class DefinitionEncoder extends MessageToByteEncoder {

	private Class<?> genericClass;
	public DefinitionEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	public void encode(ChannelHandlerContext ctx, Object inob, ByteBuf out)
			throws Exception {
		if (genericClass.isInstance(inob)) {
			byte[] data = HessianUtil.serialize(inob);
			out.writeInt(data.length);
			out.writeBytes(data);
		}
	}
}