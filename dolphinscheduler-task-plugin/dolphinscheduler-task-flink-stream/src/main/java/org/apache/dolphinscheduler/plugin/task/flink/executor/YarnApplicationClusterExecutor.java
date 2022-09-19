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

import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkResultInfo;
import org.apache.dolphinscheduler.plugin.task.flink.factory.YarnClusterDescriptorFactory;

import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YarnApplicationClusterExecutor extends AbstractClusterExecutor {

    private static final Logger logger = LoggerFactory.getLogger(YarnApplicationClusterExecutor.class);

    public YarnApplicationClusterExecutor(FlinkParamsInfo jobParamsInfo) {
        super(jobParamsInfo);
    }

    @Override
    public FlinkResultInfo submitJob() {
        try {
            Configuration flinkConfig = getFlinkConfigFromParamsInfo();
            flinkConfig.setString(
                    DeploymentOptions.TARGET, YarnDeploymentTarget.APPLICATION.getName());
            ClusterSpecification clusterSpecification =
                    YarnClusterDescriptorFactory.INSTANCE.getClusterSpecification(flinkConfig);

            ApplicationConfiguration applicationConfiguration =
                    new ApplicationConfiguration(
                            jobParamsInfo.getExecArgs(), jobParamsInfo.getEntryPointClassName());
            try (
                    YarnClusterDescriptor clusterDescriptor =
                            (YarnClusterDescriptor) YarnClusterDescriptorFactory.INSTANCE.createClusterDescriptor(
                                    jobParamsInfo.getHadoopConfDir(), flinkConfig)) {
                ClusterClientProvider<ApplicationId> application =
                        clusterDescriptor.deployApplicationCluster(
                                clusterSpecification, applicationConfiguration);
                String applicationId = application.getClusterClient().getClusterId().toString();
                return new FlinkResultInfo(applicationId, "");
            }
        } catch (Exception e) {
            logger.error("submit job to yarn error:", e);
            return new FlinkResultInfo("", "");
        }
    }
}
