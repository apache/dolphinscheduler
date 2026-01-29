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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlinkArgsUtilsTest {

    private String joinStringListWithSpace(List<String> stringList) {
        return String.join(" ", stringList);
    }

    private FlinkParameters buildTestFlinkParametersWithDeployMode(FlinkDeployMode flinkDeployMode) {
        FlinkParameters flinkParameters = new FlinkParameters();
        flinkParameters.setProgramType(ProgramType.SCALA);
        flinkParameters.setDeployMode(flinkDeployMode);
        flinkParameters.setParallelism(4);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName("/opt/job.jar");
        flinkParameters.setMainJar(resourceInfo);
        flinkParameters.setMainClass("org.example.Main");
        flinkParameters.setSlot(4);
        flinkParameters.setAppName("demo-app-name");
        flinkParameters.setJobManagerMemory("1024m");
        flinkParameters.setTaskManagerMemory("1024m");

        return flinkParameters;
    }
    private TaskExecutionContext buildTestTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("app-id");
        taskExecutionContext.setExecutePath("/tmp/execution");

        ResourceContext.ResourceItem resourceItem = new ResourceContext.ResourceItem();
        resourceItem.setResourceAbsolutePathInLocal("/opt/job.jar");
        resourceItem.setResourceAbsolutePathInStorage("/opt/job.jar");

        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(resourceItem);
        taskExecutionContext.setResourceContext(resourceContext);
        return taskExecutionContext;
    }

    @Test
    public void testRunJarInApplicationMode() throws Exception {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.APPLICATION);
        List<String> commandLine = FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run-application -t yarn-application -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -sae -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testRunJarInClusterMode() {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.CLUSTER);
        flinkParameters.setFlinkVersion("1.11");
        List<String> commandLine1 =
                FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -m yarn-cluster -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -sae -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine1));

        flinkParameters.setFlinkVersion("<1.10");
        List<String> commandLine2 =
                FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -m yarn-cluster -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -sae -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine2));

        flinkParameters.setFlinkVersion(">=1.12");
        List<String> commandLine3 =
                FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -t yarn-per-job -ys 4 -ynm demo-app-name -yjm 1024m -ytm 1024m -p 4 -sae -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine3));
    }

    @Test
    public void testRunJarInLocalMode() {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.LOCAL);
        List<String> commandLine = FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/flink run -p 4 -sae -c org.example.Main /opt/job.jar",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testRunSql() {
        FlinkParameters flinkParameters = buildTestFlinkParametersWithDeployMode(FlinkDeployMode.CLUSTER);
        flinkParameters.setProgramType(ProgramType.SQL);
        List<String> commandLine = FlinkArgsUtils.buildRunCommandLine(buildTestTaskExecutionContext(), flinkParameters);

        Assertions.assertEquals(
                "${FLINK_HOME}/bin/sql-client.sh -i /tmp/execution/app-id_init.sql -f /tmp/execution/app-id_node.sql",
                joinStringListWithSpace(commandLine));
    }

    @Test
    public void testInitOptionsInLocalMode() {
        List<String> initOptions =
                FlinkArgsUtils.buildInitOptionsForSql(buildTestFlinkParametersWithDeployMode(FlinkDeployMode.LOCAL));
        Assertions.assertEquals(2, initOptions.size());
        Assertions.assertTrue(initOptions.contains("set execution.target=local"));
        Assertions.assertTrue(initOptions.contains("set parallelism.default=4"));
    }

    @Test
    public void testInitOptionsInClusterMode() throws Exception {
        List<String> initOptions = FlinkArgsUtils
                .buildInitOptionsForSql(buildTestFlinkParametersWithDeployMode(FlinkDeployMode.CLUSTER));
        Assertions.assertEquals(6, initOptions.size());
        Assertions.assertTrue(initOptions.contains("set execution.target=yarn-per-job"));
        Assertions.assertTrue(initOptions.contains("set taskmanager.numberOfTaskSlots=4"));
        Assertions.assertTrue(initOptions.contains("set yarn.application.name=demo-app-name"));
        Assertions.assertTrue(initOptions.contains("set jobmanager.memory.process.size=1024m"));
        Assertions.assertTrue(initOptions.contains("set taskmanager.memory.process.size=1024m"));
        Assertions.assertTrue(initOptions.contains("set parallelism.default=4"));
    }
}
