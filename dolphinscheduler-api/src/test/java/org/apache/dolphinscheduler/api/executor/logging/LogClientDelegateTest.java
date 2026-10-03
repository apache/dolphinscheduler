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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.apache.dolphinscheduler.extract.base.exception.MethodInvocationException;
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
    public void testStreamWholeLogTaskInstanceNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> logClientDelegate.streamWholeLog(null,
                new ByteArrayOutputStream()));
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

    /**
     * First chunk fails with an ERROR code → fall back to remote log storage. The legacy
     * whole-file worker RPC no longer exists as a fallback: it is unbounded on the worker side
     * (see {@link LogClientDelegate#streamWholeLog}).
     */
    @Test
    public void testStreamWholeLogFirstChunkFailsFallsBackToRemote() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] remoteData = "REMOTE_ARCHIVE".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));
        mockRemoteStream(ti, remoteData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(remoteData, out.toByteArray());
        verify(remoteLogClient, times(1)).streamWholeLog(eq(ti), any(OutputStream.class));
    }

    /**
     * Old worker simulation: it does not implement the chunk RPC, so the RPC layer fails with
     * "Cannot find the ServerMethodInvoker" and the client proxy throws. Must go to remote log
     * storage — never to the legacy whole-file RPC.
     */
    @Test
    public void testStreamWholeLogRpcThrowsFallsBackToRemote() throws Exception {
        TaskInstance ti = newTaskInstance();
        byte[] remoteData = "REMOTE_ARCHIVE".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenThrow(new RuntimeException("Cannot find ServerMethodInvoker"));
        mockRemoteStream(ti, remoteData);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertArrayEquals(remoteData, out.toByteArray());
        verify(remoteLogClient, times(1)).streamWholeLog(eq(ti), any(OutputStream.class));
    }

    /**
     * Chunk RPC unavailable because the worker ANSWERED but could not dispatch the method (an
     * old worker during a rolling upgrade — the chunk method is missing, the server answers with
     * a fail response and the client proxy throws {@link MethodInvocationException}) AND remote
     * log storage cannot serve the log either → must fail with the explicit "worker upgrade
     * required" error, not silently succeed, and not attempt any unbounded whole-file fetch.
     */
    @Test
    public void testStreamWholeLogRpcThrowsAndRemoteFailsThrowsUpgradeError() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenThrow(new MethodInvocationException(
                        "Cannot find the ServerMethodInvoker of getTaskInstanceLogFileChunk"));
        doThrow(new IOException("Remote log file not found after download (remote log archiving may not be enabled "
                + "or the archive is missing): /tmp/x.log"))
                        .when(remoteLogClient).streamWholeLog(eq(ti), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOException thrown = assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertTrue(thrown.getMessage().contains("upgrade required"),
                "Old-worker failure must carry the explicit upgrade guidance, got: " + thrown.getMessage());
        assertTrue(thrown.getMessage().contains(ti.getHost()));
        // The remote failure must stay visible in the cause chain for diagnosis.
        assertTrue(thrown.getCause().getMessage().contains("Remote log file not found"));
        assertEquals(0, out.toByteArray().length);
    }

    /**
     * The worker is DOWN (or unreachable): the RPC fails with a plain transport error — no fail
     * response came back, so it is NOT an old-worker signal. The error must point operations at
     * the worker's reachability, NOT at a worker upgrade.
     */
    @Test
    public void testStreamWholeLogWorkerUnreachableAndRemoteFailsThrowsWithoutUpgradeGuidance() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenThrow(new RuntimeException("connection refused: /10.0.0.5:1235"));
        doThrow(new IOException("Remote log file not found after download (remote log archiving may not be enabled "
                + "or the archive is missing): /tmp/x.log"))
                        .when(remoteLogClient).streamWholeLog(eq(ti), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOException thrown = assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertTrue(thrown.getMessage().contains("may be down or unreachable"),
                "Unreachable worker must be reported as a reachability problem, got: " + thrown.getMessage());
        assertFalse(thrown.getMessage().contains("upgrade required"),
                "Upgrade guidance must NOT appear for a worker that never answered the RPC");
        assertTrue(thrown.getCause().getMessage().contains("Remote log file not found"));
        assertEquals(0, out.toByteArray().length);
    }

    /**
     * The worker ANSWERED with a structured non-SUCCESS code (a new worker that supports the
     * chunk RPC — e.g. transient ERROR) and remote storage also failed → the error must report
     * both failures as they are, WITHOUT the "worker upgrade required" guidance (the worker is
     * not old; that message would mislead).
     */
    @Test
    public void testStreamWholeLogStructuredFailureAndRemoteFailsThrowsWithoutUpgradeGuidance() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "down", true));
        doThrow(new IOException("Remote log file not found after download (remote log archiving may not be enabled "
                + "or the archive is missing): /tmp/x.log"))
                        .when(remoteLogClient).streamWholeLog(eq(ti), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOException thrown = assertThrows(IOException.class, () -> logClientDelegate.streamWholeLog(ti, out));
        assertTrue(thrown.getMessage().contains("Chunked log fetch failed"),
                "Structured failure must be reported as-is, got: " + thrown.getMessage());
        assertTrue(thrown.getMessage().contains("ERROR"));
        assertFalse(thrown.getMessage().contains("upgrade required"),
                "Upgrade guidance must NOT appear for a worker that answered the chunk RPC");
        assertTrue(thrown.getCause().getMessage().contains("Remote log file not found"));
        assertEquals(0, out.toByteArray().length);
    }

    /**
     * A SUCCESS chunk with ZERO bytes is the worker's authoritative "task produced no output" —
     * a valid terminal state. Must return normally without touching remote storage.
     */
    @Test
    public void testStreamWholeLogEmptyLogIsTerminal() throws Exception {
        TaskInstance ti = newTaskInstance();

        when(registryClient.checkNodeExists(eq(ti.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(ti), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileDownloadResponse(new byte[0], LogResponseStatus.SUCCESS, null,
                        true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(ti, out);

        assertEquals(0, out.toByteArray().length);
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
     * chunk RPC explicitly reporting LOG_FILE_NOT_FOUND means a NEW worker authoritatively says
     * the file is gone (an old worker cannot report this — it fails the RPC instead). The remote
     * archive may still hold the content, so go straight to remote storage.
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
        verify(remoteLogClient, times(1)).streamWholeLog(eq(ti), any(OutputStream.class));
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
