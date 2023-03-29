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

import org.apache.dolphinscheduler.remote.command.Message;
import org.apache.dolphinscheduler.remote.command.MessageContext;
import org.apache.dolphinscheduler.remote.command.MessageHeader;
import org.apache.dolphinscheduler.remote.command.MessageType;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * netty decoder
 */
@Slf4j
public class NettyDecoder extends ReplayingDecoder<NettyDecoder.State> {

    public NettyDecoder() {
        super(State.MAGIC);
    }

    private final MessageHeader messageHeader = new MessageHeader();

    /**
     * decode
     *
     * @param ctx channel handler context
     * @param in byte buffer
     * @param out out content
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case MAGIC:
                checkMagic(in.readByte());
                checkpoint(State.VERSION);
                // fallthru
            case VERSION:
                checkVersion(in.readByte());
                checkpoint(State.COMMAND);
                // fallthru
            case COMMAND:
                messageHeader.setType(in.readByte());
                checkpoint(State.OPAQUE);
                // fallthru
            case OPAQUE:
                messageHeader.setOpaque(in.readLong());
                checkpoint(State.CONTEXT_LENGTH);
                // fallthru
            case CONTEXT_LENGTH:
                messageHeader.setContextLength(in.readInt());
                checkpoint(State.CONTEXT);
                // fallthru
            case CONTEXT:
                byte[] context = new byte[messageHeader.getContextLength()];
                in.readBytes(context);
                messageHeader.setContext(context);
                checkpoint(State.BODY_LENGTH);
                // fallthru
            case BODY_LENGTH:
                messageHeader.setBodyLength(in.readInt());
                checkpoint(State.BODY);
                // fallthru
            case BODY:
                byte[] body = new byte[messageHeader.getBodyLength()];
                in.readBytes(body);
                //
                Message packet = new Message();
                packet.setType(commandType(messageHeader.getType()));
                packet.setOpaque(messageHeader.getOpaque());
                packet.setContext(MessageContext.valueOf(messageHeader.getContext()));
                packet.setBody(body);
                out.add(packet);
                //
                checkpoint(State.MAGIC);
                break;
            default:
                log.warn("unknown decoder state {}", state());
        }
    }

    /**
     * get command type
     *
     * @param type type
     */
    private MessageType commandType(byte type) {
        for (MessageType ct : MessageType.values()) {
            if (ct.ordinal() == type) {
                return ct;
            }
        }
        return null;
    }

    /**
     * check magic
     *
     * @param magic magic
     */
    private void checkMagic(byte magic) {
        if (magic != Message.MAGIC) {
            throw new IllegalArgumentException("illegal packet [magic]" + magic);
        }
    }

    /**
     * check version
     */
    private void checkVersion(byte version) {
        if (version != Message.VERSION) {
            throw new IllegalArgumentException("illegal protocol [version]" + version);
        }
    }

    enum State {
        MAGIC,
        VERSION,
        COMMAND,
        OPAQUE,
        CONTEXT_LENGTH,
        CONTEXT,
        BODY_LENGTH,
        BODY;
    }
}
