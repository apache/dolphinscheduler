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
package org.apache.dolphinscheduler.server.utils;

import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, OSUtils.class, HadoopUtils.class})
public class ProcessUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getPidsStr() throws Exception {
        int processId = 1;
        String pidList = ProcessUtils.getPidsStr(processId);
        Assert.assertNotEquals("The child process of process 1 should not be empty", pidList, "");

        PowerMockito.mockStatic(OSUtils.class);
        when(OSUtils.isMacOS()).thenReturn(true);
        when(OSUtils.exeCmd(String.format("%s -p %d", Constants.PSTREE, processId))).thenReturn(null);
        String pidListMac = ProcessUtils.getPidsStr(processId);
        Assert.assertEquals("", pidListMac);
    }

    @Test
    public void testBuildCommandStr() throws IOException {
        List<String> commands = new ArrayList<>();
        commands.add("sudo");
        commands.add("-u");
        commands.add("tenantCode");
        //allowAmbiguousCommands false
        Assert.assertEquals("sudo -u tenantCode", ProcessUtils.buildCommandStr(commands));

        //quota
        commands.clear();
        commands.add("\"sudo\"");
        Assert.assertEquals("\"sudo\"", ProcessUtils.buildCommandStr(commands));

        //allowAmbiguousCommands true
        commands.clear();
        commands.add("sudo");
        System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "false");
        Assert.assertEquals("\"sudo\"", ProcessUtils.buildCommandStr(commands));
    }

    @Test
    public void testKill() {
        //get taskExecutionContext
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();

        //process id eq 0
        taskExecutionContext.setProcessId(0);
        ProcessUtils.kill(taskExecutionContext);

        //process id not eq 0
        taskExecutionContext.setProcessId(1);
        PowerMockito.mockStatic(OSUtils.class);
        try {
            when(OSUtils.exeCmd(String.format("%s -sp %d", Constants.PSTREE, 1))).thenReturn("1111");
            when(OSUtils.exeCmd(String.format("%s -p %d", Constants.PSTREE, 1))).thenReturn("1111");
            when(OSUtils.exeCmd("sudo kill -9")).thenReturn("1111");
        } catch (Exception e) {
            e.printStackTrace();
        }
        taskExecutionContext.setHost("127.0.0.1:8888");
        taskExecutionContext.setLogPath("/log/1.log");
        ProcessUtils.kill(taskExecutionContext);
        Assert.assertEquals(1, taskExecutionContext.getProcessId());
    }

    @Test
    public void testCancelApplication() {
        List<String> appIds = new ArrayList<>();
        appIds.add("application_1585532379175_228491");
        appIds.add("application_1598885606600_3677");
        String tenantCode = "dev";
        String executePath = "/ds-exec/1/1/1";
        ExecutionStatus running = ExecutionStatus.RUNNING_EXEUTION;

        PowerMockito.mockStatic(HadoopUtils.class);
        HadoopUtils hadoop = HadoopUtils.getInstance();

        try {
            PowerMockito.whenNew(HadoopUtils.class).withAnyArguments().thenReturn(hadoop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            when(hadoop.getApplicationStatus("application_1585532379175_228491")).thenReturn(running);
            when(hadoop.getApplicationStatus("application_1598885606600_3677")).thenReturn(running);
        } catch (Exception e) {
            e.printStackTrace();
            ProcessUtils.cancelApplication(appIds, logger, tenantCode, executePath);
        }

        Assert.assertNotNull(appIds);
    }
}
