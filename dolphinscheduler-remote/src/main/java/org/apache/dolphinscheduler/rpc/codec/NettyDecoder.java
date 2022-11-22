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

import org.apache.dolphinscheduler.rpc.protocol.EventType;
import org.apache.dolphinscheduler.rpc.protocol.MessageHeader;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocol;
import org.apache.dolphinscheduler.rpc.protocol.RpcProtocolConstants;
import org.apache.dolphinscheduler.rpc.serializer.RpcSerializer;
import org.apache.dolphinscheduler.rpc.serializer.Serializer;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * NettyDecoder
 */
public class NettyDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public NettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
                          List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < RpcProtocolConstants.HEADER_LENGTH) {
            return;
        }

        byteBuf.markReaderIndex();

        short magic = byteBuf.readShort();

        if (RpcProtocolConstants.MAGIC != magic) {
            throw new IllegalArgumentException("magic number is illegal, " + magic);
        }
        byte eventType = byteBuf.readByte();
        byte version = byteBuf.readByte();
        byte serialization = byteBuf.readByte();
        long requestId = byteBuf.readLong();
        int dataLength = byteBuf.readInt();
        byte[] data = new byte[dataLength];

        RpcProtocol rpcProtocol = new RpcProtocol();

        MessageHeader header = new MessageHeader();
        header.setVersion(version);
        header.setSerialization(serialization);
        header.setRequestId(requestId);
        header.setEventType(eventType);
        header.setMsgLength(dataLength);
        byteBuf.readBytes(data);
        rpcProtocol.setMsgHeader(header);
        if (eventType != EventType.HEARTBEAT.getType()) {
            Serializer serializer = RpcSerializer.getSerializerByType(serialization);
            Object obj = serializer.deserialize(data, genericClass);
            rpcProtocol.setBody(obj);
        }
        list.add(rpcProtocol);
    }

}
