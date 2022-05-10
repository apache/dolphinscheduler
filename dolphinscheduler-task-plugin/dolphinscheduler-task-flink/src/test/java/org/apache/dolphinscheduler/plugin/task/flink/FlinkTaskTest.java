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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.commons.lang.StringUtils;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    JSONUtils.class
})
@PowerMockIgnore({"javax.*"})
public class FlinkTaskTest {

    @Test
    public void testBuildCommand() {
        String parameters = buildFlinkParameters();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        when(taskExecutionContext.getQueue()).thenReturn("default");
        FlinkTask flinkTask = spy(new FlinkTask(taskExecutionContext));
        flinkTask.init();
        Assert.assertEquals(
            "flink run " +
                "-m yarn-cluster " +
                "-ys 1 " +
                "-ynm TopSpeedWindowing " +
                "-yjm 1G " +
                "-ytm 1G " +
                "-yqu default " +
                "-p 2 -sae " +
                "-c org.apache.flink.streaming.examples.windowing.TopSpeedWindowing " +
                "TopSpeedWindowing.jar", flinkTask.buildCommand());
    }

    @Test
    public void testBuildCommandWithFlinkSql() {
        String parameters = buildFlinkParametersWithFlinkSql();
        TaskExecutionContext taskExecutionContext = PowerMockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        when(taskExecutionContext.getTaskAppId()).thenReturn("4483");
        FlinkTask flinkTask = spy(new FlinkTask(taskExecutionContext));
        flinkTask.init();
        Assert.assertEquals("sql-client.sh -i /tmp/4483_init.sql -f /tmp/4483_node.sql", flinkTask.buildCommand());
    }

    private String buildFlinkParameters() {
        ResourceInfo resource = new ResourceInfo();
        resource.setId(2);
        resource.setResourceName("/TopSpeedWindowing.jar");
        resource.setRes("TopSpeedWindowing.jar");

        FlinkParameters parameters = new FlinkParameters();
        parameters.setLocalParams(Collections.emptyList());
        parameters.setResourceList(Collections.emptyList());
        parameters.setProgramType(ProgramType.JAVA);
        parameters.setMainClass("org.apache.flink.streaming.examples.windowing.TopSpeedWindowing");
        parameters.setMainJar(resource);
        parameters.setDeployMode("cluster");
        parameters.setAppName("TopSpeedWindowing");
        parameters.setFlinkVersion(">=1.10");
        parameters.setJobManagerMemory("1G");
        parameters.setTaskManagerMemory("1G");
        parameters.setSlot(1);
        parameters.setTaskManager(2);
        parameters.setParallelism(2);
        return JSONUtils.toJsonString(parameters);
    }

    private String buildFlinkParametersWithFlinkSql() {
        FlinkParameters parameters = new FlinkParameters();
        parameters.setLocalParams(Collections.emptyList());
        parameters.setInitScript("set sql-client.execution.result-mode=tableau;");
        parameters.setRawScript("selcet 11111;");
        parameters.setProgramType(ProgramType.SQL);
        parameters.setMainClass(StringUtils.EMPTY);
        parameters.setDeployMode("cluster");
        parameters.setAppName("FlinkSQL");
        parameters.setOthers(StringUtils.EMPTY);
        parameters.setJobManagerMemory("1G");
        parameters.setTaskManagerMemory("1G");
        parameters.setParallelism(1);
        parameters.setFlinkVersion(">=1.10");
        return JSONUtils.toJsonString(parameters);
    }
}
