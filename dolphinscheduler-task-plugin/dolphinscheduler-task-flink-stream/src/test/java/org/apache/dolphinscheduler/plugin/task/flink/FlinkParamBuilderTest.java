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

import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.enums.FlinkStreamDeployMode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;

public class FlinkParamBuilderTest {

    @Test
    public void testRunJarInYarnPerMode() {

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

        FlinkParamsInfo jobParamsInfo = FlinkParamsInfo.builder()
                .execArgs(execArgs)
                .name(jobName)
                .runJarPath(runJarPath)
                .flinkConfDir(flinkConfDir)
                .confProperties(confProperties)
                .flinkJarPath(flinkJarPath)
                .hadoopConfDir(hadoopConfDir)
                .queue(queue)
                .runMode(runMode)
                .build();

        Assert.assertNotNull(jobParamsInfo);
    }

    @Test
    public void testCancelJobInYarnMode() {

        String hadoopConfDir = "/usr/local/Cellar/hadoop/3.3.3/libexec/etc/hadoop";

        FlinkParamsInfo jobParamsInfo = FlinkParamsInfo.builder()
                .hadoopConfDir(hadoopConfDir)
                .flinkJobId("job_id")
                .applicationId("app_id")
                .build();

        Assert.assertNotNull(jobParamsInfo);
    }

    @Test
    public void testSavePointInYarnMode() {

        String hadoopConfDir = "/usr/local/Cellar/hadoop/3.3.3/libexec/etc/hadoop";

        FlinkParamsInfo jobParamsInfo = FlinkParamsInfo.builder()
                .hadoopConfDir(hadoopConfDir)
                .flinkJobId("job_id")
                .applicationId("app_id")
                .build();

        Assert.assertNotNull(jobParamsInfo);
    }
}
