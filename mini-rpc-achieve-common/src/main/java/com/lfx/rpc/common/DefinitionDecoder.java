package com.lfx.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * 
 *ByteToMessageDecoder netty的解码器
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
        //获取序列化数据的长度
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        //反序列数据
        Object obj = HessianUtil.deserialize(data);
        out.add(obj);
    }
}
