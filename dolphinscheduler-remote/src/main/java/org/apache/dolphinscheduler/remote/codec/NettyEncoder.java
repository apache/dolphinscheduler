package org.apache.dolphinscheduler.remote.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.dolphinscheduler.remote.command.Command;

/**
 * @Author: Tboy
 */
@Sharable
public class NettyEncoder extends MessageToByteEncoder<Command> {

    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        if(msg == null){
            throw new Exception("encode msg is null");
        }
        out.writeByte(Command.MAGIC);
        out.writeByte(msg.getType().ordinal());
        out.writeLong(msg.getOpaque());
        out.writeInt(msg.getBody().length);
        out.writeBytes(msg.getBody());
    }

}

