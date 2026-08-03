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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileChunkResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
                new TaskInstanceLogFileDownloadResponse("logBytes".getBytes(), LogResponseStatus.SUCCESS, null));

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
                new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "error"));
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

    @Test
    public void testStreamWholeLogConcatenatesWorkerChunks() throws Exception {
        TaskInstance taskInstance = newTaskInstance("SHELL");
        byte[] full = "0123456789ABCDEFGHIJ".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(taskInstance), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, 10, 20, false))
                .thenReturn(chunk(full, 10, 10, 20, true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(taskInstance, out);

        assertArrayEquals(full, out.toByteArray());
    }

    @Test
    public void testStreamWholeLogFallsBackToRemoteWhenWorkerFails() throws Exception {
        TaskInstance taskInstance = newTaskInstance("SHELL");
        byte[] full = "REMOTE-CONTENT".getBytes(StandardCharsets.UTF_8);
        File localFile = File.createTempFile("ds-remote-test", ".log");

        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any())).thenReturn(true);
        when(localLogClient.getLogChunk(eq(taskInstance), anyLong(), anyInt()))
                .thenReturn(new TaskInstanceLogFileChunkResponse(null, false, 0, LogResponseStatus.ERROR, "down"));
        when(remoteLogClient.prepareLocalLog(taskInstance)).thenReturn(localFile);
        when(remoteLogClient.getLocalLogChunk(eq(localFile), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, full.length, full.length, true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(taskInstance, out);

        assertArrayEquals(full, out.toByteArray());
        // remote object synced exactly once (not per-chunk)
        verify(remoteLogClient, times(1)).prepareLocalLog(taskInstance);
    }

    @Test
    public void testStreamWholeLogNodeGoneStreamsFromRemote() throws Exception {
        TaskInstance taskInstance = newTaskInstance("SHELL");
        byte[] full = "DIRECT-REMOTE".getBytes(StandardCharsets.UTF_8);
        File localFile = File.createTempFile("ds-remote-test", ".log");

        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any())).thenReturn(false);
        when(remoteLogClient.prepareLocalLog(taskInstance)).thenReturn(localFile);
        when(remoteLogClient.getLocalLogChunk(eq(localFile), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, full.length, full.length, true));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        logClientDelegate.streamWholeLog(taskInstance, out);

        assertArrayEquals(full, out.toByteArray());
        verify(remoteLogClient, times(1)).prepareLocalLog(taskInstance);
    }

    /**
     * Regression: if the worker fails AFTER some bytes are already written, the delegate must NOT
     * fall back (restarting from offset 0 would duplicate the prefix and corrupt the download).
     * It must throw, and the output must contain exactly what was written before the failure.
     */
    @Test
    public void testStreamWholeLogThrowsWhenWorkerFailsAfterPartialWrite() throws Exception {
        TaskInstance taskInstance = newTaskInstance("SHELL");
        byte[] full = "0123456789ABCDEFGHIJ".getBytes(StandardCharsets.UTF_8);

        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any())).thenReturn(true);
        // first chunk succeeds (10 bytes, eof=false); second chunk fails
        when(localLogClient.getLogChunk(eq(taskInstance), anyLong(), anyInt()))
                .thenReturn(chunk(full, 0, 10, 20, false))
                .thenReturn(new TaskInstanceLogFileChunkResponse(null, false, 0, LogResponseStatus.ERROR, "down"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOException ex = assertThrows(IOException.class,
                () -> logClientDelegate.streamWholeLog(taskInstance, out));
        assertTrue(ex.getMessage().contains("after 10 bytes"),
                "Exception should mention partial write, got: " + ex.getMessage());
        assertEquals(10, out.toByteArray().length,
                "Output should contain only the first chunk (no prefix duplication)");
        // must not have fallen back to remote
        verify(remoteLogClient, never()).prepareLocalLog(any());
    }

    private static TaskInstance newTaskInstance(String taskType) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType(taskType);
        return taskInstance;
    }

    private static TaskInstanceLogFileChunkResponse chunk(byte[] full, int off, int len, int size, boolean eof) {
        byte[] b = new byte[len];
        System.arraycopy(full, off, b, 0, len);
        return new TaskInstanceLogFileChunkResponse(b, eof, size, LogResponseStatus.SUCCESS, null);
    }
}
