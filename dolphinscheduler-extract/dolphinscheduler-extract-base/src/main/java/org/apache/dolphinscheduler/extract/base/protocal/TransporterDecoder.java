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

package org.apache.dolphinscheduler.extract.base.protocal;

import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

@Slf4j
public class TransporterDecoder extends ReplayingDecoder<TransporterDecoder.State> {

    public TransporterDecoder() {
        super(State.MAGIC);
    }

    private int headerLength;
    private byte[] header;
    private int bodyLength;
    private byte[] body;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case MAGIC:
                checkMagic(in.readByte());
                checkpoint(State.VERSION);
            case VERSION:
                checkVersion(in.readByte());
                checkpoint(State.HEADER_LENGTH);
            case HEADER_LENGTH:
                headerLength = in.readInt();
                checkpoint(State.HEADER);
            case HEADER:
                header = new byte[headerLength];
                in.readBytes(header);
                checkpoint(State.BODY_LENGTH);
            case BODY_LENGTH:
                bodyLength = in.readInt();
                checkpoint(State.BODY);
            case BODY:
                body = new byte[bodyLength];
                in.readBytes(body);
                Transporter transporter =
                        Transporter.of(JsonSerializer.deserialize(header, TransporterHeader.class), body);
                out.add(transporter);
                checkpoint(State.MAGIC);
                break;
            default:
                log.warn("unknown decoder state {}", state());
        }
    }

    private void checkMagic(byte magic) {
        if (magic != Transporter.MAGIC) {
            throw new IllegalArgumentException("illegal packet [magic]" + magic);
        }
    }

    private void checkVersion(byte version) {
        if (version != Transporter.VERSION) {
            throw new IllegalArgumentException("illegal protocol [version]" + version);
        }
    }

    enum State {
        MAGIC,
        VERSION,
        HEADER_LENGTH,
        HEADER,
        BODY_LENGTH,
        BODY;
    }

}
