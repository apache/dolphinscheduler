package org.apache.dolphinscheduler.extract.base.protocal;

import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link TransporterDecoder} size validation.
 *
 * <p>Before the fix, {@code TransporterDecoder.decode()} did {@code new byte[bodyLength]}
 * without any size validation. A malicious peer could send bodyLength=Integer.MAX_VALUE
 * (~2GB), causing immediate {@link OutOfMemoryError}.
 *
 * <p>After the fix, the decoder validates headerLength and bodyLength against maxFrameSize
 * and throws {@link TooLongFrameException} if exceeded.
 */
@Slf4j
class TransporterDecoderTest {

    private static final int SMALL_MAX_FRAME_SIZE = 1024;

    /**
     * Verify that a normal packet within maxFrameSize is decoded correctly.
     */
    @Test
    void normalPacketDecodedSuccessfully() {
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("testMethod"));
        byte[] body = "hello world".getBytes();

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(header.length);
        packet.writeBytes(header);
        packet.writeInt(body.length);
        packet.writeBytes(body);

        assertTrue(channel.writeInbound(packet));

        Transporter received = channel.readInbound();
        assertNotNull(received, "Decoded transporter should not be null");
        assertEquals("testMethod", received.getHeader().getMethodIdentifier());

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that bodyLength exceeding maxFrameSize is rejected with TooLongFrameException.
     * Before the fix, this would attempt new byte[500MB] and cause OOM.
     */
    @Test
    void oversizedBodyRejected() {
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("testMethod"));

        ByteBuf maliciousPacket = Unpooled.buffer();
        maliciousPacket.writeByte(Transporter.MAGIC);
        maliciousPacket.writeByte(Transporter.VERSION);
        maliciousPacket.writeInt(header.length);
        maliciousPacket.writeBytes(header);
        maliciousPacket.writeInt(500 * 1024 * 1024); // 500MB body — far exceeds 1KB limit

        assertThrows(TooLongFrameException.class, () -> channel.writeInbound(maliciousPacket),
                "Expected TooLongFrameException for 500MB body with 1KB maxFrameSize");

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that bodyLength = Integer.MAX_VALUE (~2GB) is rejected.
     * This matches the production crash scenario.
     */
    @Test
    void maxIntBodyLengthRejected() {
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("pageQueryTaskInstanceLog"));

        ByteBuf maliciousPacket = Unpooled.buffer();
        maliciousPacket.writeByte(Transporter.MAGIC);
        maliciousPacket.writeByte(Transporter.VERSION);
        maliciousPacket.writeInt(header.length);
        maliciousPacket.writeBytes(header);
        maliciousPacket.writeInt(Integer.MAX_VALUE); // ~2GB

        assertThrows(TooLongFrameException.class, () -> channel.writeInbound(maliciousPacket),
                "Expected TooLongFrameException for Integer.MAX_VALUE body length");

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that headerLength exceeding maxFrameSize is also rejected.
     */
    @Test
    void oversizedHeaderRejected() {
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        ByteBuf maliciousPacket = Unpooled.buffer();
        maliciousPacket.writeByte(Transporter.MAGIC);
        maliciousPacket.writeByte(Transporter.VERSION);
        maliciousPacket.writeInt(500 * 1024 * 1024); // 500MB header — far exceeds 1KB limit

        assertThrows(TooLongFrameException.class, () -> channel.writeInbound(maliciousPacket),
                "Expected TooLongFrameException for 500MB header with 1KB maxFrameSize");

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that negative bodyLength is rejected.
     */
    @Test
    void negativeBodyLengthRejected() {
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("testMethod"));

        ByteBuf maliciousPacket = Unpooled.buffer();
        maliciousPacket.writeByte(Transporter.MAGIC);
        maliciousPacket.writeByte(Transporter.VERSION);
        maliciousPacket.writeInt(header.length);
        maliciousPacket.writeBytes(header);
        maliciousPacket.writeInt(-1); // negative body length

        assertThrows(TooLongFrameException.class, () -> channel.writeInbound(maliciousPacket),
                "Expected TooLongFrameException for negative body length");

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that a packet with body exactly at maxFrameSize boundary is accepted.
     */
    @Test
    void bodyAtExactMaxFrameSizeAccepted() {
        int bodySize = SMALL_MAX_FRAME_SIZE; // exactly at the limit
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("testMethod"));
        byte[] body = new byte[bodySize];

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(header.length);
        packet.writeBytes(header);
        packet.writeInt(body.length);
        packet.writeBytes(body);

        assertTrue(channel.writeInbound(packet));

        Transporter received = channel.readInbound();
        assertNotNull(received, "Decoded transporter should not be null at boundary size");
        assertEquals(bodySize, received.getBody().length);

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that default constructor uses 64MB maxFrameSize.
     */
    @Test
    void defaultConstructorUses64MB() {
        // A body of 1MB should be accepted with default constructor (64MB limit)
        TransporterDecoder decoder = new TransporterDecoder();
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("testMethod"));
        byte[] body = new byte[1024 * 1024]; // 1MB

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(header.length);
        packet.writeBytes(header);
        packet.writeInt(body.length);
        packet.writeBytes(body);

        assertTrue(channel.writeInbound(packet));

        Transporter received = channel.readInbound();
        assertNotNull(received, "1MB body should be accepted with default 64MB limit");
        assertEquals(1024 * 1024, received.getBody().length);

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that an empty body (0 bytes) is accepted.
     */
    @Test
    void emptyBodyAccepted() {
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("testMethod"));

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(header.length);
        packet.writeBytes(header);
        packet.writeInt(0); // empty body

        assertTrue(channel.writeInbound(packet));

        Transporter received = channel.readInbound();
        assertNotNull(received, "Empty body should be accepted");
        assertEquals(0, received.getBody().length);

        channel.finishAndReleaseAll();
    }

    /**
     * Verify that a body 1 byte larger than maxFrameSize is rejected.
     */
    @Test
    void bodyOneByteOverMaxFrameSizeRejected() {
        int bodySize = SMALL_MAX_FRAME_SIZE + 1; // 1 byte over the limit
        TransporterDecoder decoder = new TransporterDecoder(SMALL_MAX_FRAME_SIZE);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] header = JsonSerializer.serialize(TransporterHeader.of("testMethod"));

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(header.length);
        packet.writeBytes(header);
        packet.writeInt(bodySize);

        assertThrows(TooLongFrameException.class, () -> channel.writeInbound(packet),
                "Expected TooLongFrameException for body 1 byte over maxFrameSize");

        channel.finishAndReleaseAll();
    }
}
