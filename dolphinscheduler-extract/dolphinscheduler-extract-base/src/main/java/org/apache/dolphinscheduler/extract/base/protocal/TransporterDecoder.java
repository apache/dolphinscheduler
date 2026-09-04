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
import io.netty.handler.codec.TooLongFrameException;

@Slf4j
public class TransporterDecoder extends ReplayingDecoder<TransporterDecoder.State> {

    /**
     * Single source of truth for the default frame-size limit; the client/server configs default
     * to it. Bump it HERE — setting one side higher than the other makes that side happily send
     * frames the other rejects with TooLongFrameException.
     *
     * <p><b>Lower bound:</b> JSON-serialized payloads grow ~4/3 via base64 encoding of byte[]
     * fields (an 8 MB log chunk is ~10.7 MB on the wire). Setting this below ~4/3 of the largest
     * expected payload plus header headroom silently breaks every such RPC — each one is rejected
     * with TooLongFrameException. The default (64 MB) comfortably covers the 8 MB log chunk.
     */
    public static final int DEFAULT_MAX_FRAME_SIZE = 64 * 1024 * 1024;

    private final int maxFrameSize;

    public TransporterDecoder() {
        this(DEFAULT_MAX_FRAME_SIZE);
    }

    public TransporterDecoder(int maxFrameSize) {
        super(State.MAGIC);
        this.maxFrameSize = maxFrameSize;
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
                if (headerLength < 0 || headerLength > maxFrameSize) {
                    throw new TooLongFrameException(
                            "Header length " + headerLength + " exceeds max frame size " + maxFrameSize);
                }
                checkpoint(State.HEADER);
            case HEADER:
                header = new byte[headerLength];
                in.readBytes(header);
                checkpoint(State.BODY_LENGTH);
            case BODY_LENGTH:
                bodyLength = in.readInt();
                // maxFrameSize bounds the WHOLE message (header + body), not each field
                // separately — per-field checks alone would still allow a 2x maxFrameSize
                // total allocation. Long arithmetic: two ints summed can overflow.
                if (bodyLength < 0 || (long) headerLength + (long) bodyLength > maxFrameSize) {
                    throw new TooLongFrameException(
                            "Frame size (header " + headerLength + " + body " + bodyLength
                                    + ") exceeds max frame size " + maxFrameSize);
                }
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
