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

import org.apache.commons.lang3.SystemUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ProcessUtilsTest {

    @Test
    public void testGetPidsStr() throws Exception {
        // first
        String pids = "sudo(6279)---558_1497.sh(6282)---sleep(6354)";
        int processId = 6279;
        String exceptPidsStr = "6279 6282 6354";
        String command;
        MockedStatic<OSUtils> osUtilsMockedStatic = Mockito.mockStatic(OSUtils.class);
        if (SystemUtils.IS_OS_MAC) {
            command = String.format("%s -sp %d", TaskConstants.PSTREE, processId);
        } else if (SystemUtils.IS_OS_LINUX) {
            command = String.format("%s -p %d", TaskConstants.PSTREE, processId);
        } else {
            command = String.format("%s -p %d", TaskConstants.PSTREE, processId);
        }
        osUtilsMockedStatic.when(() -> OSUtils.exeCmd(command)).thenReturn(pids);
        String actualPidsStr = ProcessUtils.getPidsStr(processId);
        Assertions.assertEquals(exceptPidsStr, actualPidsStr);

        // second
        String pids2 = "apache2(2000)---222332-apache2-submit_task.py(2100)---apache2(2101)";
        int processId2 = 2000;
        String exceptPidsStr2 = "2000 2100 2101";
        String command2;
        if (SystemUtils.IS_OS_MAC) {
            command2 = String.format("%s -sp %d", TaskConstants.PSTREE, processId2);
        } else if (SystemUtils.IS_OS_LINUX) {
            command2 = String.format("%s -p %d", TaskConstants.PSTREE, processId2);
        } else {
            command2 = String.format("%s -p %d", TaskConstants.PSTREE, processId2);
        }
        osUtilsMockedStatic.when(() -> OSUtils.exeCmd(command2)).thenReturn(pids2);
        String actualPidsStr2 = ProcessUtils.getPidsStr(processId2);
        Assertions.assertEquals(exceptPidsStr2, actualPidsStr2);

        // Third
        String pids3 = "sshd(5000)---sshd(6000)---bash(7000)---python(7100)";
        int processId3 = 5000;
        String exceptPidsStr3 = "5000 6000 7000 7100";
        String command3;
        if (SystemUtils.IS_OS_MAC) {
            command3 = String.format("%s -sp %d", TaskConstants.PSTREE, processId3);
        } else if (SystemUtils.IS_OS_LINUX) {
            command3 = String.format("%s -p %d", TaskConstants.PSTREE, processId3);
        } else {
            command3 = String.format("%s -p %d", TaskConstants.PSTREE, processId3);
        }
        osUtilsMockedStatic.when(() -> OSUtils.exeCmd(command3)).thenReturn(pids3);
        String actualPidsStr3 = ProcessUtils.getPidsStr(processId3);
        Assertions.assertEquals(exceptPidsStr3, actualPidsStr3);
    }

}
