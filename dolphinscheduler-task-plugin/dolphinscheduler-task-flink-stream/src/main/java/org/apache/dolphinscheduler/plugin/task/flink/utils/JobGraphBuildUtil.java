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

package org.apache.dolphinscheduler.plugin.task.flink.utils;

import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkParamsInfo;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;
import org.apache.flink.util.function.FunctionUtils;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobGraphBuildUtil {

    private static final Logger logger = LoggerFactory.getLogger(JobGraphBuildUtil.class);

    public static final String SAVE_POINT_PATH_KEY = "savePointPath";
    public static final String ALLOW_NON_RESTORED_STATE_KEY = "allowNonRestoredState";
    public static final String PARALLELISM = "parallelism.default";

    public static JobGraph buildJobGraph(FlinkParamsInfo jobParamsInfo, Configuration flinkConfig) throws Exception {
        Properties confProperties = jobParamsInfo.getConfProperties();
        int parallelism =
                Objects.isNull(confProperties)
                        ? 1
                        : Integer.parseInt(confProperties.getProperty(PARALLELISM, "1"));

        // build program
        PackagedProgram.Builder builder = PackagedProgram.newBuilder();
        Optional.ofNullable(jobParamsInfo.getExecArgs()).ifPresent(builder::setArguments);
        Optional.ofNullable(jobParamsInfo.getEntryPointClassName())
                .ifPresent(builder::setEntryPointClassName);
        Optional.ofNullable(jobParamsInfo.getDependFiles())
                .map(JobGraphBuildUtil::getUserClassPath)
                .ifPresent(builder::setUserClassPaths);
        // deal user jar path
        builder.setJarFile(new File(jobParamsInfo.getRunJarPath()));
        // deal savepoint config
        Optional.ofNullable(confProperties)
                .ifPresent(
                        (properties) -> {
                            SavepointRestoreSettings savepointRestoreSettings =
                                    dealSavepointRestoreSettings(properties);
                            builder.setSavepointRestoreSettings(savepointRestoreSettings);
                        });

        PackagedProgram program = builder.build();
        try {
            JobGraph jobGraph =
                    PackagedProgramUtils.createJobGraph(program, flinkConfig, parallelism, false);
            // fixme: auto upload udf
            Optional.ofNullable(program.getClasspaths()).ifPresent(jobGraph::addJars);
            return jobGraph;
        } finally {
            program.close();
        }
    }

    private static List<URL> getUserClassPath(String[] jars) {
        List<URL> collect =
                Arrays.stream(jars)
                        .map(FunctionUtils.uncheckedFunction(URL::new))
                        .collect(Collectors.toList());

        collect.forEach(jar -> logger.info("parsed user classpath from jars:{}", jar));
        return collect;
    }

    public static Configuration getFlinkConfiguration(String flinkConfDir) {
        return StringUtils.isEmpty(flinkConfDir)
                ? new Configuration()
                : GlobalConfiguration.loadConfiguration(flinkConfDir);
    }

    private static SavepointRestoreSettings dealSavepointRestoreSettings(
                                                                         Properties confProperties) {
        SavepointRestoreSettings savepointRestoreSettings = SavepointRestoreSettings.none();
        String savePointPath = confProperties.getProperty(SAVE_POINT_PATH_KEY);
        if (StringUtils.isNotBlank(savePointPath)) {
            String allowNonRestoredState =
                    confProperties.getOrDefault(ALLOW_NON_RESTORED_STATE_KEY, "false").toString();
            savepointRestoreSettings =
                    SavepointRestoreSettings.forPath(
                            savePointPath, BooleanUtils.toBoolean(allowNonRestoredState));
        }
        return savepointRestoreSettings;
    }
}
