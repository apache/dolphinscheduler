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

package org.apache.dolphinscheduler.plugin.task.seatunnel.self;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.seatunnel.DeployModeEnum;

import org.apache.commons.io.FileUtils;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SeatunnelEngineTaskTest {

    private static final String EXECUTE_PATH = "/tmp";
    private static final String TASK_ID = "1234";
    private static final String RAW_SCRIPT =
            "env {\n  job.mode = \"BATCH\"\n}\nsource {\n  FakeSource {}\n}\nsink {\n  Console {}\n}";

    private MockedStatic<FileUtils> mockedStaticFileUtils;

    @BeforeEach
    public void setUp() {
        mockedStaticFileUtils = Mockito.mockStatic(FileUtils.class);
    }

    @AfterEach
    public void after() {
        mockedStaticFileUtils.close();
    }

    @Test
    public void buildOptionsUsesMasterForDeployMode() throws Exception {
        SeatunnelEngineParameters seatunnelParameters = newSeatunnelEngineParameters();
        seatunnelParameters.setDeployMode(DeployModeEnum.cluster);

        SeatunnelEngineTask task = newSeatunnelEngineTask(seatunnelParameters);

        String command = String.join(" ", task.buildOptions());
        String expectedCommand = String.format("--config %s/seatunnel_%s.conf --master cluster", EXECUTE_PATH, TASK_ID);
        Assertions.assertEquals(expectedCommand, command);
    }

    @Test
    public void buildOptionsOmitsMasterWhenDeployModeMissing() throws Exception {
        SeatunnelEngineParameters seatunnelParameters = newSeatunnelEngineParameters();

        SeatunnelEngineTask task = newSeatunnelEngineTask(seatunnelParameters);

        String command = String.join(" ", task.buildOptions());
        String expectedCommand = String.format("--config %s/seatunnel_%s.conf", EXECUTE_PATH, TASK_ID);
        Assertions.assertEquals(expectedCommand, command);
    }

    @Test
    public void buildOptionsAppendsOthersWhenPresent() throws Exception {
        SeatunnelEngineParameters seatunnelParameters = newSeatunnelEngineParameters();
        seatunnelParameters.setDeployMode(DeployModeEnum.local);
        seatunnelParameters.setOthers("--name demo-job");

        SeatunnelEngineTask task = newSeatunnelEngineTask(seatunnelParameters);

        String command = String.join(" ", task.buildOptions());
        String expectedCommand =
                String.format("--config %s/seatunnel_%s.conf --master local --name demo-job", EXECUTE_PATH, TASK_ID);
        Assertions.assertEquals(expectedCommand, command);
    }

    private SeatunnelEngineParameters newSeatunnelEngineParameters() {
        SeatunnelEngineParameters seatunnelParameters = new SeatunnelEngineParameters();
        seatunnelParameters.setUseCustom(true);
        seatunnelParameters.setRawScript(RAW_SCRIPT);
        return seatunnelParameters;
    }

    private SeatunnelEngineTask newSeatunnelEngineTask(SeatunnelEngineParameters seatunnelParameters) throws Exception {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setExecutePath(EXECUTE_PATH);
        taskExecutionContext.setTaskAppId(TASK_ID);
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(seatunnelParameters));

        SeatunnelEngineTask task = new SeatunnelEngineTask(taskExecutionContext);
        task.setSeatunnelParameters(seatunnelParameters);
        setPrivateSeatunnelParameters(task, seatunnelParameters);
        return task;
    }

    private void setPrivateSeatunnelParameters(SeatunnelEngineTask task,
                                               SeatunnelEngineParameters seatunnelParameters) throws Exception {
        Field field = SeatunnelEngineTask.class.getDeclaredField("seatunnelParameters");
        field.setAccessible(true);
        field.set(task, seatunnelParameters);
    }
}
