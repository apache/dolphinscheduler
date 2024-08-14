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
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class SeatunnelTaskTest {
    private static final String EXECUTE_PATH = "/home";
    private static final String TASK_APPID = "9527";

    @Test
    public void formatDetector() throws Exception{
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setRawScript(RAW_SCRIPT);

        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setExecutePath(EXECUTE_PATH);
        taskExecutionContext.setTaskAppId(TASK_APPID);
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(seatunnelParameters));

        SeatunnelTask seatunnelTask = new SeatunnelTask(taskExecutionContext);
        seatunnelTask.setSeatunnelParameters(seatunnelParameters);
        Assertions.assertEquals("/home/seatunnel_9527.conf", seatunnelTask.buildCustomConfigCommand());

        seatunnelParameters.setRawScript(RAW_SCRIPT_2);
        seatunnelTask.setSeatunnelParameters(seatunnelParameters);
        Assertions.assertEquals("/home/seatunnel_9527.json", seatunnelTask.buildCustomConfigCommand());
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