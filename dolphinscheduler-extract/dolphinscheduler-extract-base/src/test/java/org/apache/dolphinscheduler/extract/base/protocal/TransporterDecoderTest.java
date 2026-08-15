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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;

class TransporterDecoderTest {

    private static final int MAX_FRAME_SIZE = 64 * 1024 * 1024;

    @Test
    void shouldDecodeValidFrameWithinLimit() {
        TransporterHeader header = TransporterHeader.of("test-method");
        byte[] headerBytes = header.toBytes();
        byte[] bodyBytes = new byte[]{0x01, 0x02, 0x03};

        EmbeddedChannel channel = new EmbeddedChannel(new TransporterDecoder());
        channel.writeInbound(Unpooled.wrappedBuffer(encodeFrame(headerBytes, bodyBytes)));

        Transporter decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(header.getMethodIdentifier(), decoded.getHeader().getMethodIdentifier());
        assertEquals(header.getOpaque(), decoded.getHeader().getOpaque());
        assertEquals(bodyBytes.length, decoded.getBody().length);
    }

    @Test
    void shouldRejectHeaderLengthExceedingMaxFrameSize() {
        EmbeddedChannel channel = new EmbeddedChannel(new TransporterDecoder());
        byte[] headerBytes = new byte[]{0x01, 0x02};
        byte[] bodyBytes = new byte[]{0x03};

        TooLongFrameException thrown = assertThrows(TooLongFrameException.class, () -> channel.writeInbound(
                Unpooled.wrappedBuffer(encodeFrameWithCustomHeaderLength(headerBytes, bodyBytes, MAX_FRAME_SIZE + 1))));
        assertTrue(thrown.getMessage().contains("Header length"));
        assertNull(channel.readInbound());
    }

    @Test
    void shouldRejectBodyLengthExceedingMaxFrameSize() {
        TransporterHeader header = TransporterHeader.of("test-method");
        byte[] headerBytes = header.toBytes();
        byte[] bodyBytes = new byte[]{0x01};

        EmbeddedChannel channel = new EmbeddedChannel(new TransporterDecoder());
        TooLongFrameException thrown = assertThrows(TooLongFrameException.class, () -> channel.writeInbound(
                Unpooled.wrappedBuffer(encodeFrameWithCustomBodyLength(headerBytes, bodyBytes, MAX_FRAME_SIZE + 1))));
        assertTrue(thrown.getMessage().contains("exceeds max frame size"));
        assertNull(channel.readInbound());
    }

    /**
     * maxFrameSize bounds the WHOLE message (header + body), not each part separately: checking
     * each field individually would still allow a 2×maxFrameSize total allocation. Here header
     * (700B) and body (600B) each fit under the 1KB limit but sum to 1300B — must be rejected
     * BEFORE the body array is allocated.
     */
    @Test
    void shouldRejectCombinedHeaderAndBodyExceedingMaxFrameSize() {
        final int smallMaxFrameSize = 1024;
        byte[] headerBytes = new byte[700]; // fully written so decoding reaches BODY_LENGTH

        EmbeddedChannel channel = new EmbeddedChannel(new TransporterDecoder(smallMaxFrameSize));
        TooLongFrameException thrown = assertThrows(TooLongFrameException.class, () -> channel.writeInbound(
                Unpooled.wrappedBuffer(
                        encodeFrameWithCustomLengths(headerBytes, new byte[]{0x01}, headerBytes.length, 600))));
        assertTrue(thrown.getMessage().contains("exceeds max frame size"),
                "Rejection must be on the combined size, got: " + thrown.getMessage());
        assertNull(channel.readInbound());
    }

    /**
     * A frame whose header + body is EXACTLY maxFrameSize is legal — the bound is inclusive.
     */
    @Test
    void shouldAcceptFrameExactlyAtCombinedMaxFrameSize() {
        final int smallMaxFrameSize = 1024;
        TransporterHeader header = TransporterHeader.of("test-method");
        byte[] headerBytes = header.toBytes();
        // header + body == 1024 exactly
        byte[] bodyBytes = new byte[smallMaxFrameSize - headerBytes.length];

        EmbeddedChannel channel = new EmbeddedChannel(new TransporterDecoder(smallMaxFrameSize));
        channel.writeInbound(Unpooled.wrappedBuffer(encodeFrame(headerBytes, bodyBytes)));

        Transporter decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(bodyBytes.length, decoded.getBody().length);
    }

    private byte[] encodeFrame(byte[] headerBytes, byte[] bodyBytes) {
        return encodeFrameWithCustomLengths(headerBytes, bodyBytes, headerBytes.length, bodyBytes.length);
    }

    private byte[] encodeFrameWithCustomHeaderLength(byte[] headerBytes, byte[] bodyBytes, int headerLength) {
        return encodeFrameWithCustomLengths(headerBytes, bodyBytes, headerLength, bodyBytes.length);
    }

    private byte[] encodeFrameWithCustomBodyLength(byte[] headerBytes, byte[] bodyBytes, int bodyLength) {
        return encodeFrameWithCustomLengths(headerBytes, bodyBytes, headerBytes.length, bodyLength);
    }

    private byte[] encodeFrameWithCustomLengths(byte[] headerBytes, byte[] bodyBytes, int headerLength,
                                                int bodyLength) {
        byte[] frame = new byte[2 + 4 + headerBytes.length + 4 + bodyBytes.length];
        int offset = 0;
        frame[offset++] = Transporter.MAGIC;
        frame[offset++] = Transporter.VERSION;
        frame[offset++] = (byte) (headerLength >> 24);
        frame[offset++] = (byte) (headerLength >> 16);
        frame[offset++] = (byte) (headerLength >> 8);
        frame[offset++] = (byte) headerLength;
        System.arraycopy(headerBytes, 0, frame, offset, headerBytes.length);
        offset += headerBytes.length;
        frame[offset++] = (byte) (bodyLength >> 24);
        frame[offset++] = (byte) (bodyLength >> 16);
        frame[offset++] = (byte) (bodyLength >> 8);
        frame[offset++] = (byte) bodyLength;
        System.arraycopy(bodyBytes, 0, frame, offset, bodyBytes.length);
        return frame;
    }
}
