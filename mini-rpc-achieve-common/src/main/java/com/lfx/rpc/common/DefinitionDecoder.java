package com.lfx.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * 
 *ByteToMessageDecoder netty�Ľ�����
 */                                    
public class DefinitionDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public DefinitionDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }
    

    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        //��ȡ���л����ݵĳ���
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        //����������
        Object obj = HessianUtil.deserialize(data);
        out.add(obj);
    }
}
