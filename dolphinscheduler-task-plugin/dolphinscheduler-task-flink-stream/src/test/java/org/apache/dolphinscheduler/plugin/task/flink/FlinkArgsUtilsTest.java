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

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.flink.entity.ParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.ResultInfo;
import org.apache.dolphinscheduler.plugin.task.flink.enums.ClusterClient;
import org.apache.dolphinscheduler.plugin.task.flink.enums.FlinkStreamDeployMode;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkArgsUtilsTest {

    protected final Logger logger =
            LoggerFactory.getLogger(String.format(TaskConstants.TASK_LOG_LOGGER_NAME_FORMAT, getClass()));

    @Test
    public void testRunJarInYarnPerMode() throws Exception {

        String runJarPath = "/usr/local/Cellar/apache-flink/1.15.1/libexec/examples/streaming/WindowJoin.jar";

        String[] execArgs = new String[]{};

        String jobName = "Flink Submit";

        String flinkConfDir = "/usr/local/Cellar/apache-flink/1.15.1/libexec/conf";

        String flinkJarPath = "/usr/local/Cellar/apache-flink/1.15.1/libexec/lib";

        FlinkStreamDeployMode runMode = FlinkStreamDeployMode.YARN_PER_JOB;

        String queue = "root.default";

        String hadoopConfDir = "/usr/local/Cellar/hadoop/3.3.3/libexec/etc/hadoop";

        Properties confProperties = new Properties();
        confProperties.setProperty("parallelism.default", "1");
        confProperties.setProperty("jobmanager.memory.process.size", "2G");
        confProperties.setProperty("taskmanager.memory.process.size", "2G");
        confProperties.setProperty("taskmanager.numberOfTaskSlots", "2");

        ParamsInfo jobParamsInfo = ParamsInfo.builder()
                .setExecArgs(execArgs)
                .setName(jobName)
                .setRunJarPath(runJarPath)
                .setFlinkConfDir(flinkConfDir)
                .setConfProperties(confProperties)
                .setFlinkJarPath(flinkJarPath)
                .setHadoopConfDir(hadoopConfDir)
                .setQueue(queue)
                .setRunMode(runMode)
                .build();

        Assert.assertNotNull(jobParamsInfo);
    }

    @Test
    public void testCancelJobInYarnMode() throws Exception {

        String hadoopConfDir = "/usr/local/Cellar/hadoop/3.3.3/libexec/etc/hadoop";

        ParamsInfo jobParamsInfo = ParamsInfo.builder()
                .setHadoopConfDir(hadoopConfDir)
                .setFlinkJobId("job_id")
                .setApplicationId("app_id")
                .build();

        Assert.assertNotNull(jobParamsInfo);
    }

    @Test
    public void testSavePointInYarnMode() throws Exception {

        String hadoopConfDir = "/usr/local/Cellar/hadoop/3.3.3/libexec/etc/hadoop";

        ParamsInfo jobParamsInfo = ParamsInfo.builder()
                .setHadoopConfDir(hadoopConfDir)
                .setFlinkJobId("job_id")
                .setApplicationId("app_id")
                .build();

        Assert.assertNotNull(jobParamsInfo);
    }
}
