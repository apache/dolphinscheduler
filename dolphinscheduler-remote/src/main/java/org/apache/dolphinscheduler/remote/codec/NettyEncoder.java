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

import org.apache.dolphinscheduler.remote.command.Command;
import org.apache.dolphinscheduler.remote.exceptions.RemotingException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * netty encoder
 */
@Sharable
public class NettyEncoder extends MessageToByteEncoder<Command> {

    /**
     * encode
     *
     * @param ctx channel handler context
     * @param msg command
     * @param out byte buffer
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        if (msg == null) {
            throw new RemotingException("encode msg is null");
        }
        out.writeByte(Command.MAGIC);
        out.writeByte(Command.VERSION);
        out.writeByte(msg.getType().ordinal());
        out.writeLong(msg.getOpaque());
        writeContext(msg, out);
        out.writeInt(msg.getBody().length);
        out.writeBytes(msg.getBody());
    }

    private void writeContext(Command msg, ByteBuf out) {
        byte[] headerBytes = msg.getContext().toBytes();
        out.writeInt(headerBytes.length);
        out.writeBytes(headerBytes);
    }
}

