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

package org.apache.dolphinscheduler.plugin.task.api.utils;

import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ProcessUtilsTest {

    private MockedStatic<OSUtils> mockedOSUtils;

    @BeforeEach
    void setUp() {
        mockedOSUtils = Mockito.mockStatic(OSUtils.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedOSUtils != null) {
            mockedOSUtils.close();
        }
    }
    @Test
    public void testGetPidList() throws Exception {
        // first
        String pids = "sudo(6279)---558_1497.sh(6282)---sleep(6354)";
        int processId = 6279;
        List<Integer> exceptPidList = new ArrayList<>(Arrays.asList(6279, 6282, 6354));
        String command;
        if (SystemUtils.IS_OS_MAC) {
            pids = "-+= 6279 sudo -+- 6282 558_1497.sh --- 6354 sleep";
            command = String.format("%s -sp %d", TaskConstants.PSTREE, processId);
        } else if (SystemUtils.IS_OS_LINUX) {
            command = String.format("%s -p %d", TaskConstants.PSTREE, processId);
        } else {
            command = String.format("%s -p %d", TaskConstants.PSTREE, processId);
        }
        mockedOSUtils.when(() -> OSUtils.exeCmd(command)).thenReturn(pids);
        List<Integer> actualPidList = ProcessUtils.getPidList(processId);
        Assertions.assertEquals(exceptPidList, actualPidList);

        // second
        String pids2 = "apache2(2000)---222332-apache2-submit_task.py(2100)---apache2(2101)";
        int processId2 = 2000;
        List<Integer> exceptPidList2 = new ArrayList<>(Arrays.asList(2000, 2100, 2101));
        String command2;
        if (SystemUtils.IS_OS_MAC) {
            pids2 = "-+= 2000 apache2 -+- 2100 222332-apache2-submit_task.py --- 2101 apache2";
            command2 = String.format("%s -sp %d", TaskConstants.PSTREE, processId2);
        } else if (SystemUtils.IS_OS_LINUX) {
            command2 = String.format("%s -p %d", TaskConstants.PSTREE, processId2);
        } else {
            command2 = String.format("%s -p %d", TaskConstants.PSTREE, processId2);
        }
        mockedOSUtils.when(() -> OSUtils.exeCmd(command2)).thenReturn(pids2);
        List<Integer> actualPidList2 = ProcessUtils.getPidList(processId2);
        Assertions.assertEquals(exceptPidList2, actualPidList2);

        // Third
        String pids3 = "sshd(5000)---sshd(6000)---bash(7000)---python(7100)";
        int processId3 = 5000;
        List<Integer> exceptPidList3 = new ArrayList<>(Arrays.asList(5000, 6000, 7000, 7100));
        String command3;
        if (SystemUtils.IS_OS_MAC) {
            pids3 = "-+= 5000 sshd -+- 6000 sshd --= 7000 bash --- 7100 python";
            command3 = String.format("%s -sp %d", TaskConstants.PSTREE, processId3);
        } else if (SystemUtils.IS_OS_LINUX) {
            command3 = String.format("%s -p %d", TaskConstants.PSTREE, processId3);
        } else {
            command3 = String.format("%s -p %d", TaskConstants.PSTREE, processId3);
        }
        mockedOSUtils.when(() -> OSUtils.exeCmd(command3)).thenReturn(pids3);
        List<Integer> actualPidList3 = ProcessUtils.getPidList(processId3);
        Assertions.assertEquals(exceptPidList3, actualPidList3);
    }

    @Test
    public void testRemoveK8sClientCache() {
        Assertions.assertDoesNotThrow(() -> {
            ProcessUtils.removeK8sClientCache("a");
        });

        Assertions.assertThrows(Exception.class, () -> {
            ProcessUtils.removeK8sClientCache(null);
        });
    }

    @Test
    void testKillProcessSuccessWithNoAlivePids() {
        // Arrange
        TaskExecutionContext taskRequest = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskRequest.getProcessId()).thenReturn(12345);
        Mockito.when(taskRequest.getTenantCode()).thenReturn("testTenant");

        // Mock getPidsStr
        String pstreeCmd;
        String pstreeOutput;
        if (SystemUtils.IS_OS_MAC) {
            pstreeCmd = "pstree -sp 12345";
            pstreeOutput = "-+= 12345 sudo -+- 1234 86.sh";
        } else {
            pstreeCmd = "pstree -p 12345";
            pstreeOutput = "sudo(12345)---86.sh(1234)";
        }
        mockedOSUtils.when(() -> OSUtils.exeCmd(pstreeCmd)).thenReturn(pstreeOutput);

        // Mock kill -0
        mockedOSUtils.when(() -> OSUtils.getSudoCmd(Mockito.eq("testTenant"), Mockito.matches("kill -0.*")))
                .thenReturn("kill -0 12345");
        mockedOSUtils.when(() -> OSUtils.exeCmd(Mockito.matches(".*kill -0.*")))
                .thenThrow(new RuntimeException("Command failed"));

        // Act
        boolean result = ProcessUtils.kill(taskRequest);

        // Assert
        Assertions.assertTrue(result);

        // Verify SIGINT, SIGTERM, SIGKILL never called
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -2 12345 1234"), Mockito.never());
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -15 12345 1234"), Mockito.never());
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -9 12345 1234"), Mockito.never());
    }

    @Test
    void testKillProcessSuccessWithSigInt() {
        // Arrange
        TaskExecutionContext taskRequest = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskRequest.getProcessId()).thenReturn(12345);
        Mockito.when(taskRequest.getTenantCode()).thenReturn("testTenant");

        // Mock getPidsStr
        String pstreeCmd;
        String pstreeOutput;
        if (SystemUtils.IS_OS_MAC) {
            pstreeCmd = "pstree -sp 12345";
            pstreeOutput = "-+= 12345 sudo -+- 1234 86.sh";
        } else {
            pstreeCmd = "pstree -p 12345";
            pstreeOutput = "sudo(12345)---86.sh(1234)";
        }
        mockedOSUtils.when(() -> OSUtils.exeCmd(pstreeCmd)).thenReturn(pstreeOutput);

        // Mock SIGINT command
        mockedOSUtils.when(() -> OSUtils.getSudoCmd(Mockito.eq("testTenant"), Mockito.matches("kill -2.*")))
                .thenReturn("kill -2 12345 1234");
        mockedOSUtils.when(() -> OSUtils.exeCmd("kill -2 12345 1234")).thenReturn("");

        // Mock kill -0
        mockedOSUtils.when(() -> OSUtils.getSudoCmd(Mockito.eq("testTenant"), Mockito.matches("kill -0.*")))
                .thenReturn("kill -0 12345")
                .thenReturn("kill -0 1234");
        // Mock the static method OSUtils.exeCmd that matches "kill -0" command
        mockedOSUtils.when(() -> OSUtils.exeCmd(Mockito.matches(".*kill -0.*")))
                .thenReturn("") // First invocation succeeds (process is alive)
                .thenReturn("") // Second invocation succeeds (process is alive)
                // Subsequent invocations fail (process is no longer alive)
                .thenThrow(new RuntimeException("Command failed"));

        // Act
        boolean result = ProcessUtils.kill(taskRequest);

        // Assert
        Assertions.assertTrue(result);

        // Verify SIGINT was called
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -2 12345 1234"), Mockito.times(1));
        // Verify SIGTERM,SIGKILL was never called
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -15 12345 1234"), Mockito.never());
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -9 12345 1234"), Mockito.never());
    }

    @Test
    void testKillProcessFail() {
        // Arrange
        TaskExecutionContext taskRequest = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskRequest.getProcessId()).thenReturn(12345);
        Mockito.when(taskRequest.getTenantCode()).thenReturn("testTenant");

        // Mock getPidsStr
        String pstreeCmd;
        String pstreeOutput;
        if (SystemUtils.IS_OS_MAC) {
            pstreeCmd = "pstree -sp 12345";
            pstreeOutput = "-+= 12345 sudo -+- 1234 86.sh";
        } else {
            pstreeCmd = "pstree -p 12345";
            pstreeOutput = "sudo(12345)---86.sh(1234)";
        }
        mockedOSUtils.when(() -> OSUtils.exeCmd(pstreeCmd)).thenReturn(pstreeOutput);

        // Mock SIGINT command
        mockedOSUtils.when(() -> OSUtils.getSudoCmd(Mockito.eq("testTenant"), Mockito.matches("kill -2.*")))
                .thenReturn("kill -2 12345 1234");
        mockedOSUtils.when(() -> OSUtils.exeCmd("kill -2 12345 1234")).thenReturn("");

        // Mock SIGTERM command
        mockedOSUtils.when(() -> OSUtils.getSudoCmd(Mockito.eq("testTenant"), Mockito.matches("kill -15.*")))
                .thenReturn("kill -15 12345 1234");
        mockedOSUtils.when(() -> OSUtils.exeCmd("kill -15 12345 1234")).thenReturn("");

        // Mock SIGKILL command
        mockedOSUtils.when(() -> OSUtils.getSudoCmd(Mockito.eq("testTenant"), Mockito.matches("kill -9.*")))
                .thenReturn("kill -9 12345 1234");
        mockedOSUtils.when(() -> OSUtils.exeCmd("kill -9 12345 1234")).thenReturn("");

        // Mock kill -0
        mockedOSUtils.when(() -> OSUtils.getSudoCmd(Mockito.eq("testTenant"), Mockito.matches("kill -0.*")))
                .thenReturn("kill -0 12345")
                .thenReturn("kill -0 1234");
        mockedOSUtils.when(() -> OSUtils.exeCmd(Mockito.matches(".*kill -0.*"))).thenReturn("");

        // Act
        boolean result = ProcessUtils.kill(taskRequest);

        // Assert
        Assertions.assertFalse(result);

        // Verify SIGINT, SIGTERM, SIGKILL was called
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -2 12345 1234"), Mockito.times(1));
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -15 12345 1234"), Mockito.times(1));
        mockedOSUtils.verify(() -> OSUtils.exeCmd("kill -9 12345 1234"), Mockito.times(1));
    }

    @Test
    void testKillNonExistentProcess() {
        // Arrange
        TaskExecutionContext taskRequest = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskRequest.getProcessId()).thenReturn(0);

        // Act
        boolean result = ProcessUtils.kill(taskRequest);

        // Assert
        Assertions.assertTrue(result);
    }
}
