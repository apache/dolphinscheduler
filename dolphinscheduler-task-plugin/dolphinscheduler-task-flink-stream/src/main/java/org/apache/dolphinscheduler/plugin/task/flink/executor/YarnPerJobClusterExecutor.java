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

package org.apache.dolphinscheduler.plugin.task.flink.executor;

import org.apache.dolphinscheduler.plugin.task.flink.entity.ParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.ResultInfo;
import org.apache.dolphinscheduler.plugin.task.flink.factory.YarnClusterDescriptorFactory;
import org.apache.dolphinscheduler.plugin.task.flink.utils.JobGraphBuildUtil;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnPerJobClusterExecutor extends AbstractClusterExecutor {

    private static final Logger logger = LoggerFactory.getLogger(YarnPerJobClusterExecutor.class);

    public YarnPerJobClusterExecutor(ParamsInfo jobParamsInfo) {
        super(jobParamsInfo);
    }

    @Override
    public ResultInfo submitJob() {
        try {
            // 1. parse default flink configuration from flink-conf.yaml and dynamic replacement
            // default config.
            Configuration flinkConfig = getFlinkConfigFromParamsInfo();

            // 2. build JobGraph from user program.
            JobGraph jobGraph = JobGraphBuildUtil.buildJobGraph(jobParamsInfo, flinkConfig);
            logger.info("build job graph success!");

            // 3. build the submitted yarn environment.
            try (
                    YarnClusterDescriptor clusterDescriptor =
                            (YarnClusterDescriptor) YarnClusterDescriptorFactory.INSTANCE.createClusterDescriptor(
                                    jobParamsInfo.getHadoopConfDir(), flinkConfig)) {

                // 4. replace flinkJarPath and ship flink lib jars.
                replaceFlinkJarPathAndShipLibJars(
                        jobParamsInfo.getFlinkJarPath(), clusterDescriptor);

                // 5. deploy JobGraph to yarn.
                ClusterSpecification clusterSpecification =
                        YarnClusterDescriptorFactory.INSTANCE.getClusterSpecification(flinkConfig);
                ClusterClientProvider<ApplicationId> applicationIdClusterClientProvider =
                        clusterDescriptor.deployJobCluster(clusterSpecification, jobGraph, true);

                String applicationId =
                        applicationIdClusterClientProvider
                                .getClusterClient()
                                .getClusterId()
                                .toString();
                String jobId = jobGraph.getJobID().toString();
                logger.info("deploy per_job with appId: {}, jobId: {}", applicationId, jobId);

                return new ResultInfo(applicationId, jobId);
            }
        } catch (Exception e) {
            logger.error("submit job to yarn error: ", e);
            return new ResultInfo("", "");
        }
    }

    /**
     * 1. flink jar path use flinkJarPath/flink-dist.jar.
     *
     * <p>2. upload flinkJarPath jars.
     *
     * @param flinkJarPath
     * @param clusterDescriptor
     * @return
     * @throws MalformedURLException
     */
    private List<File> replaceFlinkJarPathAndShipLibJars(
                                                         String flinkJarPath,
                                                         YarnClusterDescriptor clusterDescriptor) throws MalformedURLException {
        if (StringUtils.isEmpty(flinkJarPath) || !new File(flinkJarPath).exists()) {
            throw new RuntimeException("The param '-flinkJarPath' ref dir is not exist");
        }
        File[] jars = new File(flinkJarPath).listFiles();
        List<File> shipFiles = Lists.newArrayList();
        for (File file : jars) {
            if (file.toURI().toURL().toString().contains("flink-dist")) {
                clusterDescriptor.setLocalJarPath(new Path(file.toURI().toURL().toString()));
            } else {
                shipFiles.add(file);
            }
        }

        clusterDescriptor.addShipFiles(shipFiles);
        return shipFiles;
    }
}
