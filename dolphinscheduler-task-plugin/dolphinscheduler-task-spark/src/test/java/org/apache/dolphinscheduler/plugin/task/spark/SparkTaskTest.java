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

package org.apache.dolphinscheduler.plugin.task.spark;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;

import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SparkTaskTest {

    @Test
    public void testBuildCommandWithSparkSql() throws Exception {
        String parameters = buildSparkParametersWithSparkSql();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("5536");

        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-sql " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name sparksql " +
                        "-f /tmp/5536_node.sql",
                sparkTask.getScript());
    }

    @Test
    public void testBuildCommandWithSparkSubmit() {
        String parameters = buildSparkParametersWithSparkSubmit();
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        HashMap<String, String> map = new HashMap<>();
        map.put("/lib/dolphinscheduler-task-spark.jar", "/lib/dolphinscheduler-task-spark.jar");
        Mockito.when(taskExecutionContext.getResources()).thenReturn(map);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        SparkTask sparkTask = Mockito.spy(new SparkTask(taskExecutionContext));
        sparkTask.init();
        Assertions.assertEquals(
                "${SPARK_HOME}/bin/spark-submit " +
                        "--master yarn " +
                        "--deploy-mode client " +
                        "--class org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest " +
                        "--conf spark.driver.cores=1 " +
                        "--conf spark.driver.memory=512M " +
                        "--conf spark.executor.instances=2 " +
                        "--conf spark.executor.cores=2 " +
                        "--conf spark.executor.memory=1G " +
                        "--name spark " +
                        "/lib/dolphinscheduler-task-spark.jar",
                sparkTask.getScript());
    }

    private String buildSparkParametersWithSparkSql() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setRawScript("selcet 11111;");
        sparkParameters.setProgramType(ProgramType.SQL);
        sparkParameters.setMainClass("");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("sparksql");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        return JSONUtils.toJsonString(sparkParameters);
    }

    private String buildSparkParametersWithSparkSubmit() {
        SparkParameters sparkParameters = new SparkParameters();
        sparkParameters.setLocalParams(Collections.emptyList());
        sparkParameters.setProgramType(ProgramType.SCALA);
        sparkParameters.setMainClass("org.apache.dolphinscheduler.plugin.task.spark.SparkTaskTest");
        sparkParameters.setDeployMode("client");
        sparkParameters.setAppName("spark");
        sparkParameters.setOthers("");
        sparkParameters.setDriverCores(1);
        sparkParameters.setDriverMemory("512M");
        sparkParameters.setNumExecutors(2);
        sparkParameters.setExecutorMemory("1G");
        sparkParameters.setExecutorCores(2);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setId(1);
        resourceInfo.setRes("dolphinscheduler-task-spark.jar");
        resourceInfo.setResourceName("/lib/dolphinscheduler-task-spark.jar");
        sparkParameters.setMainJar(resourceInfo);
        return JSONUtils.toJsonString(sparkParameters);
    }

}
