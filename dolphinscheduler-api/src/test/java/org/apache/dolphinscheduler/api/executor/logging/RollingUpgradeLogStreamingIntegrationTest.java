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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.config.NettyServerConfig;
import org.apache.dolphinscheduler.extract.base.server.SpringServerMethodInvokerDiscovery;
import org.apache.dolphinscheduler.extract.common.ILogService;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

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
 * Rolling-upgrade regression test for chunked log streaming.
 *
 * <p>Simulates an OLD worker (deployed before this feature) behind a real Netty RPC server and a
 * real RPC client. The old worker would happily answer the legacy whole-file RPC (it counts and
 * would return the full payload), but it does not implement the chunked log download RPC — from
 * the client's perspective this is indistinguishable from the real deployed old worker, whose
 * server replies "Cannot find the ServerMethodInvoker" for the chunk method.
 *
 * <p>The regression under test (issue #18459 review): a large log must NEVER be requested from the
 * old worker via the whole-file payload — that RPC reads the entire file into the worker's heap
 * before serialization and can OOM it, and a receiver-side maxFrameSize cannot prevent it. The
 * download must instead come from remote log storage, or fail with an explicit error.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RollingUpgradeLogStreamingIntegrationTest {

    /**
     * Larger than the 8 MB chunk size: exercises the "large log" path the reviewer is concerned
     * about.
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

    /**
     * Counts invocations of the legacy whole-file RPC on the simulated old worker. The whole-file
     * method actually WORKS on the stub (like on a real old worker) — so the assertion
     * {@code == 0} is not vacuous: a regressed delegate would get a successful response here and
     * the counter would prove it.
     */
    private final AtomicInteger wholeFileRpcInvocations = new AtomicInteger();

    @BeforeEach
    void setUp() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            nettyServerPort = s.getLocalPort();
        }

        tempLogFile = Files.createTempFile("ds-old-worker-log", ".log").toFile();
        byte[] content = new byte[FILE_SIZE];
        for (int i = 0; i < FILE_SIZE; i++) {
            content[i] = (byte) (i % 256);
        }
        Files.write(tempLogFile.toPath(), content);

        // A JDK proxy implementing the CURRENT ILogService keeps the real method identifiers
        // (method.toGenericString()) on the wire, so the RPC dispatch behaves exactly like the
        // real thing: the chunk call reaches the server and fails, and a whole-file call would
        // succeed.
        final ILogService oldWorker = (ILogService) Proxy.newProxyInstance(
                ILogService.class.getClassLoader(),
                new Class<?>[]{ILogService.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getTaskInstanceLogFileChunk":
                            // Old worker does not implement the chunked log download RPC. A real
                            // old worker fails with "Cannot find the ServerMethodInvoker"; throwing
                            // here is wire-equivalent from the client's perspective (both come back
                            // as a failed RPC → MethodInvocationException).
                            throw new UnsupportedOperationException(
                                    "simulated old worker: chunked log RPC not implemented");
                        case "getTaskInstanceWholeLogFileBytes":
                            wholeFileRpcInvocations.incrementAndGet();
                            return new TaskInstanceLogFileDownloadResponse(
                                    Files.readAllBytes(tempLogFile.toPath()), LogResponseStatus.SUCCESS, "", true);
                        default:
                            return null;
                    }
                });
        springServerMethodInvokerDiscovery = new SpringServerMethodInvokerDiscovery(
                NettyServerConfig.builder().serverName("TestOldWorkerLogServer").listenPort(nettyServerPort).build());
        springServerMethodInvokerDiscovery.registerServerMethodInvokerProvider(oldWorker);
        springServerMethodInvokerDiscovery.start();
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

    private TaskInstance newTaskInstance() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("127.0.0.1:" + nettyServerPort);
        taskInstance.setLogPath(tempLogFile.getAbsolutePath());
        taskInstance.setTaskType("SHELL");
        return taskInstance;
    }

    /**
     * Rolling upgrade, old worker, remote log storage AVAILABLE: the large log is streamed from
     * remote storage and the old worker is NEVER asked for the whole-file payload.
     */
    @Test
    void oldWorkerLargeLogStreamedFromRemoteStorageNeverWholeFileRpc() throws Exception {
        final TaskInstance taskInstance = newTaskInstance();
        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any(RegistryNodeType.class)))
                .thenReturn(true);
        final byte[] remoteData = "REMOTE_ARCHIVE".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            out.write(remoteData);
            return null;
        }).when(remoteLogClient).streamWholeLog(eq(taskInstance), any(OutputStream.class));

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(taskInstance, out);

        assertArrayEquals(remoteData, out.toByteArray());
        verify(remoteLogClient, times(1)).streamWholeLog(eq(taskInstance), any(OutputStream.class));
        assertEquals(0, wholeFileRpcInvocations.get(),
                "the old worker must NEVER be asked for the whole-file payload on the large-log path");
    }

    /**
     * Rolling upgrade, old worker, remote log storage UNAVAILABLE (not enabled / archive missing,
     * the default deployment): the download fails with the explicit "worker upgrade required"
     * error — and still never asks the old worker for the whole-file payload.
     */
    @Test
    void oldWorkerLargeLogWithoutRemoteArchiveFailsWithExplicitUpgradeError() throws Exception {
        final TaskInstance taskInstance = newTaskInstance();
        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any(RegistryNodeType.class)))
                .thenReturn(true);
        doThrow(new IOException("Remote log file not found after download (remote log archiving may not be enabled "
                + "or the archive is missing): " + taskInstance.getLogPath()))
                        .when(remoteLogClient).streamWholeLog(eq(taskInstance), any(OutputStream.class));

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final IOException thrown = assertThrows(IOException.class,
                () -> logClientDelegate.streamWholeLog(taskInstance, out));

        assertTrue(thrown.getMessage().contains("upgrade required"),
                "Error must contain the explicit upgrade guidance, got: " + thrown.getMessage());
        assertEquals(0, out.toByteArray().length);
        assertEquals(0, wholeFileRpcInvocations.get(),
                "the old worker must NEVER be asked for the whole-file payload on the large-log path");
    }
}
