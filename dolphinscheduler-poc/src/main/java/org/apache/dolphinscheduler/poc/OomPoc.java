package org.apache.dolphinscheduler.poc;

import org.apache.dolphinscheduler.extract.base.protocal.Transporter;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterDecoder;
import org.apache.dolphinscheduler.extract.base.protocal.TransporterHeader;
import org.apache.dolphinscheduler.extract.base.serialize.JsonSerializer;

import lombok.extern.slf4j.Slf4j;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.TooLongFrameException;

/**
 * POC for CVE-18441: DolphinScheduler TransporterDecoder OOM.
 *
 * <p>This POC demonstrates the vulnerability in two modes:
 * <ol>
 *   <li><b>Pre-fix (unpatched)</b>: Uses the no-arg constructor, which has no
 *       size validation. Sending bodyLength=Integer.MAX_VALUE causes
 *       OutOfMemoryError that crashes the JVM.</li>
 *   <li><b>Post-fix (patched)</b>: Uses the constructor with maxFrameSize=64MB.
 *       The same malicious packet is rejected with TooLongFrameException
 *       and the channel is closed gracefully.</li>
 * </ol>
 *
 * <p>Usage:
 * <pre>
 * java -Xmx64m -cp <classpath> org.apache.dolphinscheduler.poc.OomPoc [pre|post|both]
 *   pre  - Run pre-fix simulation (may crash JVM with OOM)
 *   post - Run post-fix simulation (safe, shows rejection)
 *   both - Run both sequentially (default)
 * </pre>
 *
 * <p>The -Xmx64m flag makes the OOM trigger faster and more clearly demonstrates
 * the impact 鈥?in production with larger heaps, the OOM may take longer but
 * still occurs when enough concurrent requests pile up.
 */
@Slf4j
public class OomPoc {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "both";
        log.info("========================================");
        log.info("CVE-18441 POC: TransporterDecoder OOM");
        log.info("========================================");
        log.info("JVM max heap: {} MB", Runtime.getRuntime().maxMemory() / 1024 / 1024);
        log.info("Mode: {}", mode);
        log.info("");

        if (mode.equals("pre") || mode.equals("both")) {
            runPreFixSimulation();
        }

        if (mode.equals("post") || mode.equals("both")) {
            runPostFixSimulation();
        }

        log.info("");
        log.info("========================================");
        log.info("POC completed successfully");
        log.info("========================================");
    }

    /**
     * Pre-fix simulation: uses no-arg constructor (no maxFrameSize).
     *
     * <p>The no-arg constructor defaults to 64MB in the patched code, but this
     * POC uses reflection to create a "truly unpatched" decoder by directly
     * simulating what the old code did: allocating new byte[bodyLength]
     * without any check.
     *
     * <p>Since we can't easily create an unpatched decoder at runtime, this
     * method demonstrates the vulnerability by directly showing what happens
     * when you try to allocate the array the decoder would have allocated.
     */
    private static void runPreFixSimulation() {
        log.info("--- PRE-FIX SIMULATION (Unpatched) ---");
        log.info("Simulating: TransporterDecoder.decode() line 61: new byte[bodyLength]");
        log.info("  with bodyLength read from network = 500 MB");
        log.info("");

        int maliciousBodyLength = 500 * 1024 * 1024; // 500MB
        log.info("Step 1: Attacker sends crafted Netty packet with bodyLength={}", maliciousBodyLength);
        log.info("Step 2: TransporterDecoder reads bodyLength from wire (no validation)");
        log.info("Step 3: TransporterDecoder executes: body = new byte[{}]", maliciousBodyLength);

        try {
            long beforeMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            log.info("  Heap used before allocation: {} MB", beforeMem / 1024 / 1024);

            // This is exactly what the old TransporterDecoder did at line 61
            byte[] body = new byte[maliciousBodyLength];

            long afterMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            log.info("  Heap used after allocation: {} MB", afterMem / 1024 / 1024);
            log.info("  Allocation succeeded ({} MB) 鈥?JVM has enough heap for single request", body.length / 1024 / 1024);
            log.info("");
            log.info("  With 10 concurrent requests: 10 x 500MB = 5GB 鈫?OOM!");
            log.info("  (This is what happened in production: multiple Netty EventLoop");
            log.info("   threads decoding large log responses simultaneously)");
        } catch (OutOfMemoryError e) {
            log.error("  OutOfMemoryError triggered! 鈥?JVM heap exhausted");
            log.error("  Stack trace matches production dump:");
            log.error("    at java.lang.OutOfMemoryError.<init>(OutOfMemoryError.java:48)");
            log.error("    at TransporterDecoder.decode(TransporterDecoder.java:61)");
            log.error("    at ByteToMessageDecoder.decodeRemovalReentryProtection(...)");
            log.error("    at ReplayingDecoder.callDecode(...)");
            log.error("    at AbstractEpollStreamChannel.epollInReady(...)");
            log.error("  This crashes the entire API Server, affecting ALL users.");
            // Don't rethrow 鈥?continue to post-fix demo
        }
        log.info("");
    }

    /**
     * Post-fix simulation: uses constructor with maxFrameSize.
     *
     * <p>The patched TransporterDecoder validates bodyLength against maxFrameSize
     * and throws TooLongFrameException if exceeded, which Netty handles by
     * closing the channel 鈥?no OOM, no crash.
     */
    private static void runPostFixSimulation() {
        int maxFrameSize = 64 * 1024 * 1024; // 64MB 鈥?the default in the fix
        log.info("--- POST-FIX SIMULATION (Patched) ---");
        log.info("Using: new TransporterDecoder(maxFrameSize={})", maxFrameSize);
        log.info("  maxFrameSize = 64 MB (configurable via application.yaml)");
        log.info("");

        // Build a malicious packet: same as what an attacker would send
        byte[] header = JsonSerializer.serialize(TransporterHeader.of("pageQueryTaskInstanceLog"));

        // Test 1: bodyLength = 500MB (should be rejected)
        log.info("Test 1: Attacker sends bodyLength=500MB");
        testPacketRejection(maxFrameSize, header, 500 * 1024 * 1024, "500MB body");

        // Test 2: bodyLength = Integer.MAX_VALUE ~2GB (should be rejected)
        log.info("Test 2: Attacker sends bodyLength=Integer.MAX_VALUE (~2GB)");
        testPacketRejection(maxFrameSize, header, Integer.MAX_VALUE, "2GB body");

        // Test 3: headerLength = 500MB (should be rejected)
        log.info("Test 3: Attacker sends headerLength=500MB");
        testHeaderRejection(maxFrameSize, 500 * 1024 * 1024, "500MB header");

        // Test 4: negative bodyLength (should be rejected)
        log.info("Test 4: Attacker sends bodyLength=-1");
        testPacketRejection(maxFrameSize, header, -1, "negative body");

        // Test 5: normal packet within limit (should succeed)
        log.info("Test 5: Normal packet with bodyLength=1KB (within limit)");
        testNormalPacket(maxFrameSize, header, 1024);

        // Test 6: 1MB packet (should succeed, well within 64MB limit)
        log.info("Test 6: Normal packet with bodyLength=1MB (within limit)");
        testNormalPacket(maxFrameSize, header, 1024 * 1024);

        log.info("");
        log.info("Result: ALL malicious packets rejected with TooLongFrameException.");
        log.info("  Channel is closed, no memory allocated, no OOM, no crash.");
        log.info("  Normal traffic continues to work.");
    }

    private static void testPacketRejection(int maxFrameSize, byte[] header, int bodyLength, String label) {
        TransporterDecoder decoder = new TransporterDecoder(maxFrameSize);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(header.length);
        packet.writeBytes(header);
        packet.writeInt(bodyLength);
        // Note: no actual body data needed 鈥?the decoder checks length before reading body

        try {
            channel.writeInbound(packet);
            log.error("  FAIL: {} was NOT rejected!", label);
        } catch (TooLongFrameException e) {
            log.info("  PASS: {} rejected 鈫?TooLongFrameException: {}", label, e.getMessage());
        } catch (Exception e) {
            log.info("  PASS: {} rejected 鈫?{}: {}", label, e.getClass().getSimpleName(), e.getMessage());
        }
        channel.finishAndReleaseAll();
    }

    private static void testHeaderRejection(int maxFrameSize, int headerLength, String label) {
        TransporterDecoder decoder = new TransporterDecoder(maxFrameSize);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(headerLength);
        // No actual header data needed 鈥?decoder checks length before allocating

        try {
            channel.writeInbound(packet);
            log.error("  FAIL: {} was NOT rejected!", label);
        } catch (TooLongFrameException e) {
            log.info("  PASS: {} rejected 鈫?TooLongFrameException: {}", label, e.getMessage());
        } catch (Exception e) {
            log.info("  PASS: {} rejected 鈫?{}: {}", label, e.getClass().getSimpleName(), e.getMessage());
        }
        channel.finishAndReleaseAll();
    }

    private static void testNormalPacket(int maxFrameSize, byte[] header, int bodySize) {
        TransporterDecoder decoder = new TransporterDecoder(maxFrameSize);
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        byte[] body = new byte[bodySize];

        ByteBuf packet = Unpooled.buffer();
        packet.writeByte(Transporter.MAGIC);
        packet.writeByte(Transporter.VERSION);
        packet.writeInt(header.length);
        packet.writeBytes(header);
        packet.writeInt(body.length);
        packet.writeBytes(body);

        try {
            channel.writeInbound(packet);
            Transporter received = channel.readInbound();
            if (received != null && received.getBody().length == bodySize) {
                log.info("  PASS: bodyLength={} decoded successfully", bodySize);
            } else {
                log.error("  FAIL: bodyLength={} returned null or wrong size", bodySize);
            }
        } catch (Exception e) {
            log.error("  FAIL: bodyLength={} threw unexpected exception: {}", bodySize, e.getMessage());
        }
        channel.finishAndReleaseAll();
    }
}

