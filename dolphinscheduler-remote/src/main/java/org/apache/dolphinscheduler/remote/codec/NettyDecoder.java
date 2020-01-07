package org.apache.dolphinscheduler.remote.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandHeader;
import org.apache.dolphinscheduler.remote.command.CommandType;

import java.util.List;

/**
 * @Author: Tboy
 */
public class NettyDecoder extends ReplayingDecoder<NettyDecoder.State> {

    public NettyDecoder(){
        super(State.MAGIC);
    }

    private final CommandHeader commandHeader = new CommandHeader();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()){
            case MAGIC:
                checkMagic(in.readByte());
                checkpoint(State.COMMAND);
            case COMMAND:
                commandHeader.setType(in.readByte());
                checkpoint(State.OPAQUE);
            case OPAQUE:
                commandHeader.setOpaque(in.readLong());
                checkpoint(State.BODY_LENGTH);
            case BODY_LENGTH:
                commandHeader.setBodyLength(in.readInt());
                checkpoint(State.BODY);
            case BODY:
                byte[] body = new byte[commandHeader.getBodyLength()];
                in.readBytes(body);
                //
                Command packet = new Command();
                packet.setType(commandType(commandHeader.getType()));
                packet.setOpaque(commandHeader.getOpaque());
                packet.setBody(body);
                out.add(packet);
                //
                checkpoint(State.MAGIC);
        }
    }

    private CommandType commandType(byte type){
        for(CommandType ct : CommandType.values()){
            if(ct.ordinal() == type){
                return ct;
            }
        }
        return null;
    }

    private void checkMagic(byte magic) {
        if (magic != Command.MAGIC) {
            throw new IllegalArgumentException("illegal packet [magic]" + magic);
        }
    }

    enum State{
        MAGIC,
        COMMAND,
        OPAQUE,
        BODY_LENGTH,
        BODY;
    }
}
