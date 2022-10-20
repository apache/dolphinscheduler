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

package org.apache.dolphinscheduler.rpc.codec;

import org.apache.dolphinscheduler.rpc.protocol.MessageHeader;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;
import org.apache.dolphinscheduler.rpc.serializer.RpcSerializer;
import org.apache.dolphinscheduler.rpc.serializer.Serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * NettyEncoder
 */
public class NettyEncoder extends MessageToByteEncoder<RpcProtocol<Object>> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol<Object> msg,
                          ByteBuf byteBuf) throws Exception {
        MessageHeader msgHeader = msg.getMsgHeader();
        byteBuf.writeShort(msgHeader.getMagic());
        byteBuf.writeByte(msgHeader.getEventType());
        byteBuf.writeByte(msgHeader.getVersion());
        byteBuf.writeByte(msgHeader.getSerialization());
        byteBuf.writeLong(msgHeader.getRequestId());
        byte[] data = new byte[0];
        int msgLength = msgHeader.getMsgLength();
        Serializer rpcSerializer = RpcSerializer.getSerializerByType(msgHeader.getSerialization());
        if (null != rpcSerializer) {
            data = rpcSerializer.serialize(msg.getBody());
            msgLength = data.length;
        }
        byteBuf.writeInt(msgLength);
        byteBuf.writeBytes(data);
    }
}
