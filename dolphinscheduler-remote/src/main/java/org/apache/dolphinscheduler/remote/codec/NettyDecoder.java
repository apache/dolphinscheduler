/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.remote.codec;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.command.CommandHeader;
import org.apache.dolphinscheduler.remote.command.CommandType;

import java.util.List;

/**
 *  netty decoder
 */
public class NettyDecoder extends ReplayingDecoder<NettyDecoder.State> {

    public NettyDecoder(){
        super(State.MAGIC);
    }

    private final CommandHeader commandHeader = new CommandHeader();

    /**
     * decode
     *
     * @param ctx channel handler context
     * @param in byte buffer
     * @param out out content
     * @throws Exception
     */
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

    /**
     *  get command type
     * @param type type
     * @return
     */
    private CommandType commandType(byte type){
        for(CommandType ct : CommandType.values()){
            if(ct.ordinal() == type){
                return ct;
            }
        }
        return null;
    }

    /**
     *  check magic
     * @param magic magic
     */
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
