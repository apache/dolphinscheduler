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

package org.apache.dolphinscheduler.server.worker.task.flink;

import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.PlaceholderUtils;
import org.apache.dolphinscheduler.common.utils.placeholder.PropertyPlaceholderHelper;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ParameterUtils.class, PlaceholderUtils.class, PropertyPlaceholderHelper.class})
public class FlinkTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(FlinkTaskTest.class);

    private TaskExecutionContext taskExecutionContext;

    private ApplicationContext applicationContext;

    private ProcessService processService;

    private FlinkTask flinkTask;

    String flink1Params = "{"
            + "\"mainArgs\":\"-j flink-connector-jdbc_2.11-1.13.0.jar\", "
            + "\"slot\":\"1\", "
            + "\"parallelism\":\"1\", "
            + "\"taskManager\":\"1\", "
            + "\"jobManagerMemory\":\"1G\", "
            + "\"taskManagerMemory\":\"2G\", "
            + "\"programType\":\"SQL\", "
            + "\"mainClass\":\"\", "
            + "\"deployMode\":\"cluster\", "
            + "\"mainJar\":{\"res\":\"\"}, "
            + "\"flinkVersion\":\">=1.10\", "
            + "\"localParams\":[], "
            + "\"others\":\"-f flinkDemo.sql\", "
            + "\"resourceList\":[{\"id\":\"1\",\"res\":\"flink-connector-jdbc_2.11-1.13.0.jar\" },"
            + "{\"id\":\"2\",\"res\":\"flinkDemo.sql\" }]"
            + "}";

    @Before
    public void setTaskExecutionContext() {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(flink1Params);
        taskExecutionContext.setQueue("dev");
        taskExecutionContext.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        taskExecutionContext.setTenantCode("1");
        taskExecutionContext.setEnvFile(".dolphinscheduler_env.sh");
        taskExecutionContext.setStartTime(new Date());
        taskExecutionContext.setTaskTimeout(0);

        processService = Mockito.mock(ProcessService.class);
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        flinkTask = new FlinkTask(taskExecutionContext, logger);
        flinkTask.init();
    }

    @Test
    public void testFlinkTaskBuildSqlCommand() {

        String flinkSqlCommand = flinkTask.buildCommand();
        String flinkSqlExpected = "sql-client.sh -f flinkDemo.sql -j flink-connector-jdbc_2.11-1.13.0.jar";
        Assert.assertEquals(flinkSqlExpected, flinkSqlCommand);
    }
}