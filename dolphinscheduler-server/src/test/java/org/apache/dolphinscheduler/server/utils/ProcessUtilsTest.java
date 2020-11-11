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

import org.apache.dolphinscheduler.common.utils.OSUtils;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, OSUtils.class})
public class ProcessUtilsTest {

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
        when(OSUtils.exeCmd("pstree -sp " + processId)).thenReturn(null);
        String pidListMac = ProcessUtils.getPidsStr(processId);
        Assert.assertEquals(pidListMac, "");
    }

    @Test
    public void testBuildCommandStr() {
        List<String> commands = new ArrayList<>();
        commands.add("sudo");
        commands.add("-u");
        commands.add("tenantCode");
        //allowAmbiguousCommands false
        Assert.assertEquals(ProcessUtils.buildCommandStr(commands), "sudo -u tenantCode");

        //quota
        commands.clear();
        commands.add("\"sudo\"");
        Assert.assertEquals(ProcessUtils.buildCommandStr(commands), "\"sudo\"");

        //allowAmbiguousCommands true
        commands.clear();
        commands.add("sudo");
        System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "false");
        Assert.assertEquals(ProcessUtils.buildCommandStr(commands), "\"sudo\"");
    }

}
