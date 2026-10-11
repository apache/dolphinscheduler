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

package org.apache.dolphinscheduler.api.executor.logging;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;
import org.apache.dolphinscheduler.extract.common.service.impl.LogServiceImpl;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Integration test for chunked log streaming.
 *
 * <p>Starts an embedded Netty RPC server running the real {@code LogServiceImpl}, writes a log
 * file larger than the 8 MB chunk size, and verifies that {@code LogClientDelegate} streams the
 * whole file back in multiple chunks, reassembling it byte-for-byte. This exercises the real RPC
 * serialization of offset/length/eof and the real chunked file read end-to-end — something the
 * unit tests (which mock the RPC) cannot cover.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogStreamingIntegrationTest {

    /**
     * Slightly larger than {@code LogServiceImpl.MAX_CHUNK_SIZE} (8 MB) so the file is guaranteed
     * to be split into at least two chunks.
     */
    private static final int FILE_SIZE = 9 * 1024 * 1024;

    @Spy
    private LocalLogClient localLogClient;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private RemoteLogClient remoteLogClient;

    @InjectMocks
    private LogClientDelegate logClientDelegate;

    private SpringServerMethodInvokerDiscovery springServerMethodInvokerDiscovery;

    private int nettyServerPort = 18080;

    private java.io.File tempLogFile;

    @BeforeEach
    void setUp() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            nettyServerPort = s.getLocalPort();
        }

        springServerMethodInvokerDiscovery = new SpringServerMethodInvokerDiscovery(
                NettyServerConfig.builder().serverName("TestChunkedLogServer").listenPort(nettyServerPort).build());
        springServerMethodInvokerDiscovery.registerServerMethodInvokerProvider(new LogServiceImpl());
        springServerMethodInvokerDiscovery.start();

        tempLogFile = Files.createTempFile("ds-chunked-log", ".log").toFile();
        byte[] content = new byte[FILE_SIZE];
        for (int i = 0; i < FILE_SIZE; i++) {
            content[i] = (byte) (i % 256);
        }
        Files.write(tempLogFile.toPath(), content);
    }

    @AfterEach
    void tearDown() {
        if (springServerMethodInvokerDiscovery != null) {
            springServerMethodInvokerDiscovery.close();
        }
        if (tempLogFile != null) {
            tempLogFile.delete();
        }
    }

    @Test
    void streamWholeLog_shouldDownloadLargeLogInMultipleChunks() throws Exception {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath(tempLogFile.getAbsolutePath());
        taskInstance.setTaskType("SHELL");

        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any(RegistryNodeType.class))).thenReturn(true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(taskInstance, outputStream);

        byte[] expected = Files.readAllBytes(tempLogFile.toPath());
        assertArrayEquals(expected, outputStream.toByteArray());

        // The file is 9 MB and the chunk size is 8 MB, so at least two chunk RPCs should have been
        // issued against the real embedded worker.
        verify(localLogClient, atLeast(2)).getLogChunk(eq(taskInstance), any(Long.class), any(Integer.class));
    }

    /**
     * End-to-end regression for log ROTATION during a download: the worker-side file shrinks
     * underneath an in-flight stream (logback fixed-window roll renames the active file and a
     * fresh one starts from 0). The download must FAIL EXPLICITLY — silently returning the
     * bytes written so far would hand the user a truncated file that looks complete.
     *
     * <p>Deterministic timing via a gate in the OutputStream: the streaming thread blocks inside
     * the first 8 MB write until the test thread has performed the rotation, so the second chunk
     * request is guaranteed to observe the shrunk file. No sleeps, no race.
     */
    @Test
    void streamWholeLog_logRotatedMidDownload_failsExplicitly() throws Exception {
        final Path logFile = tempLogFile.toPath();
        final Path rotatedFile = tempLogFile.toPath().resolveSibling(tempLogFile.getName() + ".1");

        final CountDownLatch firstChunkArrived = new CountDownLatch(1);
        final CountDownLatch rotationDone = new CountDownLatch(1);
        final AtomicLong written = new AtomicLong();

        final OutputStream gatedOutput = new OutputStream() {

            private final AtomicBoolean firstWrite = new AtomicBoolean(true);

            @Override
            public void write(final int b) {
                written.incrementAndGet();
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                written.addAndGet(len);
                if (firstWrite.compareAndSet(true, false)) {
                    // First chunk (8 MB) has arrived — hold the stream while the log rotates.
                    firstChunkArrived.countDown();
                    try {
                        if (!rotationDone.await(15, TimeUnit.SECONDS)) {
                            throw new IOException("Test gate timed out waiting for rotation");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Test gate interrupted", e);
                    }
                }
            }
        };

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(2);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath(tempLogFile.getAbsolutePath());
        taskInstance.setTaskType("SHELL");
        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any(RegistryNodeType.class))).thenReturn(true);

        final ExecutorService pool = Executors.newSingleThreadExecutor();
        final Future<Throwable> download;
        try {
            download = pool.submit(() -> {
                try {
                    logClientDelegate.streamWholeLog(taskInstance, gatedOutput);
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            });

            // Wait until the first 8 MB chunk was written, then rotate like logback would:
            // rename the active file away, start a fresh (much smaller) one at the same path.
            assertTrue(firstChunkArrived.await(15, TimeUnit.SECONDS),
                    "First chunk never arrived — streaming did not start");
            Files.move(logFile, rotatedFile, StandardCopyOption.REPLACE_EXISTING);
            Files.write(logFile, "fresh file after rotation".getBytes(StandardCharsets.UTF_8));
            rotationDone.countDown();
        } finally {
            pool.shutdown();
        }

        final Throwable thrown = download.get(30, TimeUnit.SECONDS);
        assertNotNull(thrown, "Download must fail after rotation — not return a truncated file");
        assertTrue(thrown instanceof IOException, "Expected IOException but got: " + thrown);
        assertTrue(causeChainContains(thrown, "truncated"),
                "Error chain must identify truncation (LOG_TRUNCATED propagated from the worker), got: "
                        + thrown);
        // Exactly the first 8 MB chunk was delivered before the failure.
        assertEquals(8 * 1024 * 1024, written.get());

        Files.deleteIfExists(rotatedFile);
    }

    private static boolean causeChainContains(final Throwable throwable, final String needle) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(needle)) {
                return true;
            }
            final Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
        }
        return false;
    }
}
