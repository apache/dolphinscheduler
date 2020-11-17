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

package org.apache.dolphinscheduler.server.worker.task.spark;

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
public class SparkTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(SparkTaskTest.class);

    private TaskExecutionContext taskExecutionContext;

    private ApplicationContext applicationContext;

    private ProcessService processService;

    private SparkTask spark2Task;

    String spark1Params = "{"
        + "\"mainArgs\":\"\", "
        + "\"driverMemory\":\"1G\", "
        + "\"executorMemory\":\"2G\", "
        + "\"programType\":\"SCALA\", "
        + "\"mainClass\":\"basicetl.GlobalUserCar\", "
        + "\"driverCores\":\"2\", "
        + "\"deployMode\":\"cluster\", "
        + "\"executorCores\":2, "
        + "\"mainJar\":{\"res\":\"test-1.0-SNAPSHOT.jar\"}, "
        + "\"sparkVersion\":\"SPARK1\", "
        + "\"numExecutors\":\"10\", "
        + "\"localParams\":[], "
        + "\"others\":\"\", "
        + "\"resourceList\":[]"
        + "}";

    String spark2Params = "{"
        + "\"mainArgs\":\"\", "
        + "\"driverMemory\":\"1G\", "
        + "\"executorMemory\":\"2G\", "
        + "\"programType\":\"SCALA\", "
        + "\"mainClass\":\"basicetl.GlobalUserCar\", "
        + "\"driverCores\":\"2\", "
        + "\"deployMode\":\"cluster\", "
        + "\"executorCores\":2, "
        + "\"mainJar\":{\"res\":\"test-1.0-SNAPSHOT.jar\"}, "
        + "\"sparkVersion\":\"SPARK2\", "
        + "\"numExecutors\":\"10\", "
        + "\"localParams\":[], "
        + "\"others\":\"\", "
        + "\"resourceList\":[]"
        + "}";

    @Before
    public void setTaskExecutionContext() {
        taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskParams(spark2Params);
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

        spark2Task = new SparkTask(taskExecutionContext, logger);
        spark2Task.init();
    }

    @Test
    public void testSparkTaskInit() {

        TaskExecutionContext sparkTaskCtx = new TaskExecutionContext();
        SparkTask sparkTask = new SparkTask(sparkTaskCtx, logger);
        sparkTask.init();
        sparkTask.getParameters();
        Assert.assertNull(sparkTaskCtx.getTaskParams());

        String spark2Command = spark2Task.buildCommand();
        String spark2Expected = "${SPARK_HOME2}/bin/spark-submit --master yarn --deploy-mode cluster "
            + "--class basicetl.GlobalUserCar --driver-cores 2 --driver-memory 1G --num-executors 10 "
            + "--executor-cores 2 --executor-memory 2G --queue dev test-1.0-SNAPSHOT.jar";
        Assert.assertEquals(spark2Expected, spark2Command);

        taskExecutionContext.setTaskParams(spark1Params);

        SparkTask spark1Task = new SparkTask(taskExecutionContext, logger);
        spark1Task.init();
        String spark1Command = spark1Task.buildCommand();
        String spark1Expected = "${SPARK_HOME1}/bin/spark-submit --master yarn --deploy-mode cluster "
            + "--class basicetl.GlobalUserCar --driver-cores 2 --driver-memory 1G --num-executors 10 "
            + "--executor-cores 2 --executor-memory 2G --queue dev test-1.0-SNAPSHOT.jar";
        Assert.assertEquals(spark1Expected, spark1Command);
    }
}
