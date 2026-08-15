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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.netty.handler.codec.TooLongFrameException;

@ExtendWith(MockitoExtension.class)
public class LogClientDelegateTest {

    @Mock
    private LocalLogClient localLogClient;

    @Mock
    private RemoteLogClient remoteLogClient;

    @Mock
    private RegistryClient registryClient;

    @InjectMocks
    private LogClientDelegate logClientDelegate;

    @Test
    public void testGetPartLogStringTaskInstanceNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> logClientDelegate.getPartLogString(null, 0, 10));
    }

    @Test
    public void testGetPartLogStringNodeExistsLocalSuccess() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SHELL");
        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any())).thenReturn(true);
        when(localLogClient.getPartLog(taskInstance, 0, 10))
                .thenReturn(new TaskInstanceLogPageQueryResponse("logContent", LogResponseStatus.SUCCESS, ""));
        String result = logClientDelegate.getPartLogString(taskInstance, 0, 10);
        assertEquals("logContent", result);
    }

    @Test
    public void testGetPartLogStringNodeExistsLocalFailure() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SHELL");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.WORKER)).thenReturn(true);
        when(localLogClient.getPartLog(taskInstance, 0, 10)).thenReturn(
                new TaskInstanceLogPageQueryResponse(null, LogResponseStatus.ERROR, "error"));
        when(remoteLogClient.getPartLog(taskInstance, 0, 10)).thenReturn("remoteLogContent");

        String result = logClientDelegate.getPartLogString(taskInstance, 0, 10);
        assertEquals("remoteLogContent", result);
    }

    @Test
    public void testGetPartLogStringNodeNotExists() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SHELL");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.WORKER)).thenReturn(false);
        when(remoteLogClient.getPartLog(taskInstance, 0, 10)).thenReturn("remoteLogContent");

        String result = logClientDelegate.getPartLogString(taskInstance, 0, 10);
        assertEquals("remoteLogContent", result);
    }

    @Test
    public void testGetWholeLogBytesTaskInstanceNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> logClientDelegate.getWholeLogBytes(null));
    }

    @Test
    public void testGetWholeLogBytesNodeExistsLocalSuccess() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SWITCH");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.MASTER)).thenReturn(true);
        when(localLogClient.getWholeLog(taskInstance)).thenReturn(
                new TaskInstanceLogFileDownloadResponse("logBytes".getBytes(), LogResponseStatus.SUCCESS, null, true));

        byte[] result = logClientDelegate.getWholeLogBytes(taskInstance);
        assertArrayEquals("logBytes".getBytes(), result);
    }

    @Test
    public void testGetWholeLogBytesNodeExistsLocalFailure() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SWITCH");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.MASTER)).thenReturn(true);
        when(localLogClient.getWholeLog(taskInstance)).thenReturn(
                new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "error", true));
        when(remoteLogClient.getWholeLog(taskInstance)).thenReturn("remoteLogBytes".getBytes());

        byte[] result = logClientDelegate.getWholeLogBytes(taskInstance);
        assertArrayEquals("remoteLogBytes".getBytes(), result);
    }

    @Test
    public void testGetWholeLogBytesNodeNotExists() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SWITCH");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.MASTER)).thenReturn(false);
        when(remoteLogClient.getWholeLog(taskInstance)).thenReturn("remoteLogBytes".getBytes());

        byte[] result = logClientDelegate.getWholeLogBytes(taskInstance);
        assertArrayEquals("remoteLogBytes".getBytes(), result);
    }

    // ==================== streamWholeLog tests ====================

    private static TaskInstance newTaskInstance() {
        TaskInstance ti = new TaskInstance();
        ti.setId(1);
        ti.setHost("localhost");
        ti.setTaskType("SHELL");
        return ti;
    }

    @Test
    public void testStreamWholeLogChunkSuccessToEof() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] full = "0123456789ABCDEFGHIJ".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, 10, false))
                .thenReturn(chunk(full, 10, 10, true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(full, out.toByteArray());
    }

    @Test
    public void testStreamWholeLogFirstChunkFailsFallsBackToLegacyWholeFileRpc() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] legacyData = "LEGACY_WHOLE_FILE".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));
        when(localLogClient.getWholeLog(ti))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(legacyData, LogResponseStatus.SUCCESS, "ok", true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(legacyData, out.toByteArray());
        verify(localLogClient, times(1)).getWholeLog(ti);
        verify(remoteLogClient, never()).streamWholeLog(any(), any(OutputStream.class));
    }

    @Test
    public void testStreamWholeLogNodeGoneFallsBackToRemote() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] remoteData = "REMOTE".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(false);
        mockRemoteStream(ti, remoteData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(remoteData, out.toByteArray());
        verify(localLogClient, never()).getLogChunk(any(), anyLong(), anyInt());
        verify(remoteLogClient, times(1)).streamWholeLog(eq(ti), any(OutputStream.class));
    }

    @Test
    public void testStreamWholeLogMidStreamFailureThrows() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] full = "0123456789ABCDEFGHIJ".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, 10, false))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertEquals(10, out.toByteArray().length);
        verify(remoteLogClient, never()).streamWholeLog(any(), any(OutputStream.class));
    }

    private static TaskInstanceLogFileDownloadResponse chunk(byte[] full, int off, int len, boolean eof) {
        byte[] b = new byte[len];
        System.arraycopy(full, off, b, 0, len);
        return new TaskInstanceLogFileDownloadResponse(b, LogResponseStatus.SUCCESS, null, eof);
    }

    /**
     * Old worker doesn't implement getTaskInstanceLogFileChunk → chunk RPC throws.
     * Legacy whole-file RPC also fails → must cascade to remote log storage.
     */
    @Test
    public void testStreamWholeLogRpcThrowsFallsBackToLegacyThenRemote() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] remoteData = "REMOTE_ARCHIVE".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        // Simulate old worker: chunk RPC method-not-found
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenThrow(new RuntimeException("Cannot find ServerMethodInvoker"));
        when(localLogClient.getWholeLog(ti)).thenThrow(new RuntimeException("legacy worker unreachable"));
        mockRemoteStream(ti, remoteData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(remoteData, out.toByteArray());
        verify(localLogClient, times(1)).getWholeLog(ti);
        verify(remoteLogClient, times(1)).streamWholeLog(eq(ti), any(OutputStream.class));
    }

    /**
     * Legacy SUCCESS with an EMPTY body is the worker's authoritative answer ("task produced no
     * output") — a valid terminal state. It must NOT fall through to remote storage, which would
     * turn a legitimate empty log into an error once no archive exists.
     */
    @Test
    public void testStreamWholeLogLegacyEmptySuccessIsTerminal() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));
        when(localLogClient.getWholeLog(ti))
                .thenReturn(
                        new TaskInstanceLogFileDownloadResponse(new byte[0], LogResponseStatus.SUCCESS, "ok", true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertEquals(0, out.toByteArray().length);
        verify(remoteLogClient, never()).streamWholeLog(any(), any(OutputStream.class));
    }

    /**
     * chunk RPC explicitly reporting LOG_FILE_NOT_FOUND means a NEW worker authoritatively
     * says the file is gone. Asking the SAME worker's legacy RPC afterwards is pointless and
     * ambiguous — an old/legacy response for a missing file is SUCCESS+empty (it swallows the
     * FileNotFoundException), which the SUCCESS-is-terminal semantics would then serve as an
     * empty log while the remote archive still holds the full content. Must go straight to
     * remote storage, skipping legacy entirely.
     */
    @Test
    public void testStreamWholeLogChunkNotFoundGoesStraightToRemote() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] remoteData = "REMOTE_ARCHIVE".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.LOG_FILE_NOT_FOUND,
                        "missing", true));
        mockRemoteStream(ti, remoteData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(remoteData, out.toByteArray());
        verify(localLogClient, never()).getWholeLog(any());
        verify(remoteLogClient, times(1)).streamWholeLog(eq(ti), any(OutputStream.class));
    }

    /**
     * Legacy LOG_FILE_NOT_FOUND (worker-side log cleaned up) must fall through to remote
     * storage — the archive may still exist even though the worker's local copy is gone.
     */
    @Test
    public void testStreamWholeLogLegacyFileNotFoundFallsBackToRemote() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] remoteData = "REMOTE_ARCHIVE".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));
        when(localLogClient.getWholeLog(ti))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.LOG_FILE_NOT_FOUND,
                        "missing", true));
        mockRemoteStream(ti, remoteData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(remoteData, out.toByteArray());
        verify(remoteLogClient, times(1)).streamWholeLog(eq(ti), any(OutputStream.class));
    }

    /**
     * If the legacy whole-file RPC fails because the response exceeds the configured maxFrameSize,
     * the error must be propagated explicitly as an IOException rather than being swallowed and
     * reported as a missing remote log.
     */
    @Test
    public void testStreamWholeLogLegacyTooLargePropagatesExplicitly() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));
        // Simulate the RPC framework rejecting an oversized whole-file response.
        when(localLogClient.getWholeLog(ti)).thenThrow(new RuntimeException(
                "Frame too large",
                new TooLongFrameException("Body length 67108865 exceeds max frame size 67108864")));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOException thrown = assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertTrue(thrown.getMessage().contains("exceeds the maximum legacy download size"));
        verify(remoteLogClient, never()).streamWholeLog(any(), any(OutputStream.class));
    }

    /**
     * Worker node is gone AND remote log storage has nothing (archive missing) → must throw
     * IOException so a missing log is not reported as a successful header-only download.
     */
    @Test
    public void testStreamWholeLogThrowsWhenRemoteLogMissing() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(false);
        doThrow(new IOException("Log not available")).when(remoteLogClient).streamWholeLog(eq(ti),
                any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertEquals(0, out.toByteArray().length);
        verify(localLogClient, never()).getLogChunk(any(), anyLong(), anyInt());
    }

    /**
     * RPC throws mid-stream (after bytes already written) → must throw, not fallback.
     */
    @Test
    public void testStreamWholeLogRpcThrowsMidStreamThrows() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] full = "0123456789ABCDEFGHIJ".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        // First chunk succeeds, second RPC throws
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, 10, false))
                .thenThrow(new RuntimeException("Connection reset"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertEquals(10, out.toByteArray().length);
        verify(remoteLogClient, never()).streamWholeLog(any(), any(OutputStream.class));
    }

    /**
     * Log rotation/truncation DURING the download (worker reports offset beyond the file's
     * current size) must fail explicitly — silently returning what was written so far would
     * hand the user a truncated file that looks complete.
     */
    @Test
    public void testStreamWholeLogLogRotatedMidStreamThrows() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] full = "0123456789ABCDEFGHIJ".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        // First chunk succeeds, then the file is rotated underneath the reader.
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, 10, false))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.LOG_TRUNCATED,
                        "Log file was truncated/rotated: size 0 < requested offset 10", true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertEquals(10, out.toByteArray().length);
        verify(remoteLogClient, never()).streamWholeLog(any(), any(OutputStream.class));
    }

    /**
     * Regression: when the first chunk fails and we fall back to remote storage, the
     * remote file must be streamed to the OutputStream in chunks rather than loaded
     * into a single byte[].
     */
    @Test
    public void testStreamWholeLogRemoteFallbackIsChunked() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));

        // Count how many times the mocked remote stream writes to the output stream.
        // A byte[]-based implementation would write once; a chunked stream writes many times.
        final int[] writeCallCount = {0};
        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            // Simulate a 3-chunk stream of a large log.
            for (int i = 0; i < 3; i++) {
                out.write(new byte[]{0x01, 0x02});
                writeCallCount[0]++;
            }
            return null;
        }).when(remoteLogClient).streamWholeLog(eq(ti), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertEquals(3, writeCallCount[0]);
        assertEquals(6, out.toByteArray().length);
    }

    private void mockRemoteStream(TaskInstance ti, byte[] data) throws IOException {
        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            out.write(data);
            return null;
        }).when(remoteLogClient).streamWholeLog(eq(ti), any(OutputStream.class));
    }
}
