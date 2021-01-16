package org.apache.dolphinscheduler.remote.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import org.apache.dolphinscheduler.remote.serialize.ProtoStuffUtils;

import java.util.List;

/**
 * NettyDecoder
 */
public class NettyDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;


    public NettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dataLength = byteBuf.readInt();
        if (dataLength < 0) {
            channelHandlerContext.close();
        }
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
        }
        //将ByteBuf转换为byte[]
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        //将data转换成object
        Object obj = ProtoStuffUtils.deserialize(data, genericClass);
        list.add(obj);
    }
}
