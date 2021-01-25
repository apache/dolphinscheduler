package org.apache.dolphinscheduler.remote.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import org.apache.dolphinscheduler.remote.serialize.ProtoStuffUtils;

/**
 * @author jiangli
 * @date 2021-01-12 18:52
 */
public class NettyEncoder extends MessageToByteEncoder {


    private Class<?> genericClass;

    public NettyEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(o)) {
            byte[] data = ProtoStuffUtils.serialize(o);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }

    }
}
