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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;

import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class SeatunnelTaskTest {

    private static final String EXECUTE_PATH = "/tmp";
    private static final String RESOURCE_SCRIPT_PATH = "/tmp/demo.conf";

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
    public void formatDetector() throws Exception {
        String taskId = "1234";
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(true);
        seatunnelParameters.setRawScript(RAW_SCRIPT);

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setExecutePath(EXECUTE_PATH);
        taskExecutionContext.setTaskAppId(taskId);
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(seatunnelParameters));

        SeatunnelTask seatunnelTask = new SeatunnelTask(taskExecutionContext);
        seatunnelTask.setSeatunnelParameters(seatunnelParameters);
        String command1 = String.join(" ", seatunnelTask.buildOptions());
        String expectedCommand1 = String.format("--config %s/seatunnel_%s.conf", EXECUTE_PATH, taskId);
        Assertions.assertEquals(expectedCommand1, command1);

        seatunnelParameters.setRawScript(RAW_SCRIPT_2);
        seatunnelTask.setSeatunnelParameters(seatunnelParameters);
        String command2 = String.join(" ", seatunnelTask.buildOptions());
        String expectedCommand2 = String.format("--config %s/seatunnel_%s.json", EXECUTE_PATH, taskId);
        Assertions.assertEquals(expectedCommand2, command2);
    }

    @Test
    public void testReadConfigFromResourceCenter() throws Exception {
        String taskId = "2345";
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(false);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName(RESOURCE_SCRIPT_PATH);
        seatunnelParameters.setResourceList(Collections.singletonList(resourceInfo));

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setExecutePath(EXECUTE_PATH);
        taskExecutionContext.setTaskAppId(taskId);
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(seatunnelParameters));
        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(new ResourceContext.ResourceItem(RESOURCE_SCRIPT_PATH, RESOURCE_SCRIPT_PATH));
        taskExecutionContext.setResourceContext(resourceContext);

        SeatunnelTask seatunnelTask = new SeatunnelTask(taskExecutionContext);
        seatunnelTask.setSeatunnelParameters(seatunnelParameters);
        String command = String.join(" ", seatunnelTask.buildOptions());
        String expectedCommand = String.format("--config %s/seatunnel_%s.conf", EXECUTE_PATH, taskId);
        Assertions.assertEquals(expectedCommand, command);
    }

    @Test
    public void testParameterPass() throws Exception {
        String taskId = "3456";
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(false);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName(RESOURCE_SCRIPT_PATH);
        List<Property> localParam = new ArrayList<>();
        Property property = new Property("key1", Direct.IN, DataType.VARCHAR, "value1");
        localParam.add(property);
        seatunnelParameters.setLocalParams(localParam);
        seatunnelParameters.setResourceList(Collections.singletonList(resourceInfo));

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setExecutePath(EXECUTE_PATH);
        taskExecutionContext.setTaskAppId(taskId);
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(seatunnelParameters));
        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(new ResourceContext.ResourceItem(RESOURCE_SCRIPT_PATH, RESOURCE_SCRIPT_PATH));
        taskExecutionContext.setResourceContext(resourceContext);
        taskExecutionContext.setPrepareParamsMap(Collections.singletonMap("key1", property));

        SeatunnelTask seatunnelTask = new SeatunnelTask(taskExecutionContext);
        seatunnelTask.setSeatunnelParameters(seatunnelParameters);
        String command = String.join(" ", seatunnelTask.buildOptions());
        String expectedCommand = String.format("--config %s/seatunnel_%s.conf -i key1='value1'", EXECUTE_PATH, taskId);
        Assertions.assertEquals(expectedCommand, command);
    }

    private static final String RAW_SCRIPT = "env {\n" +
            "  execution.parallelism = 2\n" +
            "  job.mode = \"BATCH\"\n" +
            "  checkpoint.interval = 10000\n" +
            "}\n" +
            "\n" +
            "source {\n" +
            "  FakeSource {\n" +
            "    parallelism = 2\n" +
            "    result_table_name = \"fake\"\n" +
            "    row.num = 16\n" +
            "    schema = {\n" +
            "      fields {\n" +
            "        name = \"string\"\n" +
            "        age = \"int\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "sink {\n" +
            "  Console {\n" +
            "  }\n" +
            "}";
    private static final String RAW_SCRIPT_2 = "{\n" +
            "  \"env\": {\n" +
            "    \"execution.parallelism\": 2,\n" +
            "    \"job.mode\": \"BATCH\",\n" +
            "    \"checkpoint.interval\": 10000\n" +
            "  },\n" +
            "  \"source\": {\n" +
            "    \"FakeSource\": {\n" +
            "      \"parallelism\": 2,\n" +
            "      \"result_table_name\": \"fake\",\n" +
            "      \"row.num\": 16,\n" +
            "      \"schema\": {\n" +
            "        \"fields\": {\n" +
            "          \"name\": \"string\",\n" +
            "          \"age\": \"int\"\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"sink\": {\n" +
            "    \"Console\": {}\n" +
            "  }\n" +
            "}";
}
