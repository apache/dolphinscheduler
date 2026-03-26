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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.common.transportor.LogResponseStatus;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogFileDownloadResponse;
import org.apache.dolphinscheduler.extract.common.transportor.TaskInstanceLogPageQueryResponse;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

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
        assertThrows(IllegalArgumentException.class, () -> logClientDelegate.getTaskLogString(null, 0, 10));
    }

    @Test
    public void testGetPartLogStringNodeExistsLocalSuccess() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SHELL");
        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any())).thenReturn(true);
        when(localLogClient.getTaskLog(taskInstance, 0, 10))
                .thenReturn(new TaskInstanceLogPageQueryResponse("logContent", LogResponseStatus.SUCCESS, ""));
        String result = logClientDelegate.getTaskLogString(taskInstance, 0, 10);
        assertEquals("logContent", result);
    }

    @Test
    public void testGetTaskOutputStringNodeExistsLocalSuccess() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SHELL");
        when(registryClient.checkNodeExists(eq(taskInstance.getHost()), any())).thenReturn(true);
        when(localLogClient.getTaskOutput(taskInstance, 0, 10))
                .thenReturn(new TaskInstanceLogPageQueryResponse("outputContent", LogResponseStatus.SUCCESS, ""));

        String result = logClientDelegate.getTaskOutputString(taskInstance, 0, 10);
        assertEquals("outputContent", result);
    }

    @Test
    public void testGetPartLogStringNodeExistsLocalFailure() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SHELL");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.WORKER)).thenReturn(true);
        when(localLogClient.getTaskLog(taskInstance, 0, 10)).thenReturn(
                new TaskInstanceLogPageQueryResponse(null, LogResponseStatus.ERROR, "error"));
        when(remoteLogClient.getTaskLogString(taskInstance, 0, 10)).thenReturn("remoteLogContent");

        String result = logClientDelegate.getTaskLogString(taskInstance, 0, 10);
        assertEquals("remoteLogContent", result);
    }

    @Test
    public void testGetPartLogStringNodeNotExists() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SHELL");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.WORKER)).thenReturn(false);
        when(remoteLogClient.getTaskLogString(taskInstance, 0, 10)).thenReturn("remoteLogContent");

        String result = logClientDelegate.getTaskLogString(taskInstance, 0, 10);
        assertEquals("remoteLogContent", result);
    }

    @Test
    public void testGetWholeLogBytesTaskInstanceNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> logClientDelegate.getTaskLogBytes(null));
    }

    @Test
    public void testGetWholeLogBytesNodeExistsLocalSuccess() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SWITCH");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.MASTER)).thenReturn(true);
        when(localLogClient.getTaskLog(taskInstance)).thenReturn(
                new TaskInstanceLogFileDownloadResponse("logBytes".getBytes(), LogResponseStatus.SUCCESS, null));

        byte[] result = logClientDelegate.getTaskLogBytes(taskInstance);
        assertArrayEquals("logBytes".getBytes(), result);
    }

    @Test
    public void testGetWholeLogBytesNodeExistsLocalFailure() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SWITCH");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.MASTER)).thenReturn(true);
        when(localLogClient.getTaskLog(taskInstance)).thenReturn(
                new TaskInstanceLogFileDownloadResponse(null, LogResponseStatus.ERROR, "error"));
        when(remoteLogClient.getTaskLogBytes(taskInstance)).thenReturn("remoteLogBytes".getBytes());

        byte[] result = logClientDelegate.getTaskLogBytes(taskInstance);
        assertArrayEquals("remoteLogBytes".getBytes(), result);
    }

    @Test
    public void testGetWholeLogBytesNodeNotExists() {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setHost("localhost");
        taskInstance.setTaskType("SWITCH");

        when(registryClient.checkNodeExists("localhost", RegistryNodeType.MASTER)).thenReturn(false);
        when(remoteLogClient.getTaskLogBytes(taskInstance)).thenReturn("remoteLogBytes".getBytes());

        byte[] result = logClientDelegate.getTaskLogBytes(taskInstance);
        assertArrayEquals("remoteLogBytes".getBytes(), result);
    }
}
