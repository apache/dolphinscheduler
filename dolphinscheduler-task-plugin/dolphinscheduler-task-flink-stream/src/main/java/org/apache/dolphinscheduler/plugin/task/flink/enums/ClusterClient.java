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

package org.apache.dolphinscheduler.plugin.task.flink.enums;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.dolphinscheduler.plugin.task.flink.client.IClusterClient;
import org.apache.dolphinscheduler.plugin.task.flink.entity.CheckpointInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkResultInfo;
import org.apache.dolphinscheduler.plugin.task.flink.executor.KerberosSecurityContext;
import org.apache.dolphinscheduler.plugin.task.flink.executor.YarnApplicationClusterExecutor;
import org.apache.dolphinscheduler.plugin.task.flink.executor.YarnPerJobClusterExecutor;
import org.apache.dolphinscheduler.plugin.task.flink.factory.YarnClusterDescriptorFactory;
import org.apache.dolphinscheduler.plugin.task.flink.utils.HdfsUtil;
import org.apache.dolphinscheduler.plugin.task.flink.utils.YarnLogHelper;
import org.apache.flink.util.Preconditions;
import org.apache.flink.util.function.FunctionUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.FinalApplicationStatus;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public enum ClusterClient implements IClusterClient {

    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ClusterClient.class);

    private static Cache<String, YarnClient> YARN_CLIENT_CACHE =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(12, TimeUnit.HOURS)
                    .removalListener(new YarnClientRemovalListener())
                    .build();

    @Override
    public FlinkResultInfo submitFlinkJob(FlinkParamsInfo jobParamsInfo) throws Exception {
        FlinkStreamDeployMode mode =
                jobParamsInfo.getRunMode() != null
                        ? FlinkStreamDeployMode.YARN_PER_JOB
                        : jobParamsInfo.getRunMode();
        FlinkResultInfo resultInfo;
        switch (mode) {
            case YARN_APPLICATION:
                resultInfo = new YarnApplicationClusterExecutor(jobParamsInfo).submitJob();
                break;
            case YARN_PER_JOB:
            default:
                resultInfo = new YarnPerJobClusterExecutor(jobParamsInfo).submitJob();
        }
        return resultInfo;
    }

    @Override
    public FlinkResultInfo submitFlinkJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo,
                FunctionUtils.uncheckedSupplier(() -> submitFlinkJob(jobParamsInfo)));
    }

    @Override
    public FlinkResultInfo killYarnJob(FlinkParamsInfo jobParamsInfo) throws IOException {
        return new YarnPerJobClusterExecutor(jobParamsInfo).killJob();
    }

    @Override
    public FlinkResultInfo killYarnJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo, FunctionUtils.uncheckedSupplier(() -> killYarnJob(jobParamsInfo)));
    }

    @Override
    public YarnTaskStatus getYarnJobStatus(FlinkParamsInfo jobParamsInfo) throws Exception {
        String applicationId = jobParamsInfo.getApplicationId();
        String hadoopConfDir = jobParamsInfo.getHadoopConfDir();
        Preconditions.checkNotNull(applicationId, "yarn applicationId is not null!");
        Preconditions.checkNotNull(hadoopConfDir, "hadoop conf dir is not null!");

        YarnClient yarnClient =
                YARN_CLIENT_CACHE.get(
                        hadoopConfDir,
                        () -> {
                            try {
                                logger.info("create yarn client,create time:{}", LocalDateTime.now());
                                return new YarnPerJobClusterExecutor(jobParamsInfo)
                                        .createYarnClient();
                            } catch (IOException e) {
                                logger.error("create yarn client error!", e);
                            }
                            return null;
                        });

        if (!Objects.isNull(yarnClient)) {
            try {
                ApplicationId appId = ApplicationId.fromString(applicationId);
                ApplicationReport report = yarnClient.getApplicationReport(appId);

                YarnApplicationState yarnApplicationState = report.getYarnApplicationState();
                switch (yarnApplicationState) {
                    case NEW:
                    case NEW_SAVING:
                    case SUBMITTED:
                    case ACCEPTED:
                        return YarnTaskStatus.ACCEPTED;
                    case RUNNING:
                        return YarnTaskStatus.RUNNING;
                    case FINISHED:
                        FinalApplicationStatus finalApplicationStatus =
                                report.getFinalApplicationStatus();
                        if (finalApplicationStatus == FinalApplicationStatus.FAILED) {
                            return YarnTaskStatus.FAILED;
                        } else if (finalApplicationStatus == FinalApplicationStatus.KILLED) {
                            return YarnTaskStatus.KILLED;
                        } else {
                            // UNDEFINED define SUCCEEDED
                            return YarnTaskStatus.SUCCEEDED;
                        }
                    case FAILED:
                        return YarnTaskStatus.FAILED;
                    case KILLED:
                        return YarnTaskStatus.KILLED;
                    default:
                        throw new RuntimeException("Unsupported application state");
                }
            } catch (Exception e) {
                logger.error("get yarn job status error!", e);
                return YarnTaskStatus.NOT_FOUND;
            }
        }
        return YarnTaskStatus.NOT_FOUND;
    }

    @Override
    public YarnTaskStatus getYarnJobStatusWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo,
                FunctionUtils.uncheckedSupplier(() -> getYarnJobStatus(jobParamsInfo)));
    }

    @Override
    public List<CheckpointInfo> getCheckpointPaths(FlinkParamsInfo jobParamsInfo) throws Exception {
        YarnConfiguration yarnConfiguration =
                YarnClusterDescriptorFactory.INSTANCE.parseYarnConfFromConfDir(
                        jobParamsInfo.getHadoopConfDir());
        FileSystem fileSystem = FileSystem.get(yarnConfiguration);

        return HdfsUtil.listFiles(
                fileSystem,
                new Path(jobParamsInfo.getHdfsPath()),
                (Path file) -> file.getName().startsWith("chk-"));
    }

    @Override
    public List<CheckpointInfo> getCheckpointPathsWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo,
                FunctionUtils.uncheckedSupplier(() -> getCheckpointPaths(jobParamsInfo)));
    }

    @Override
    public String printFinishedLogToFile(FlinkParamsInfo jobParamsInfo) throws Exception {
        String applicationId = jobParamsInfo.getApplicationId();
        String finishedJobLogDir = jobParamsInfo.getFinishedJobLogDir();

        YarnConfiguration yarnConfiguration =
                YarnClusterDescriptorFactory.INSTANCE.parseYarnConfFromConfDir(
                        jobParamsInfo.getHadoopConfDir());

        return YarnLogHelper.printAllContainersLogsReturnFilePath(
                yarnConfiguration, finishedJobLogDir, applicationId);
    }

    @Override
    public String printFinishedLogToFileWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo,
                FunctionUtils.uncheckedSupplier(() -> printFinishedLogToFile(jobParamsInfo)));
    }

    @Override
    public FlinkResultInfo cancelFlinkJob(FlinkParamsInfo jobParamsInfo) throws Exception {
        Preconditions.checkNotNull(
                jobParamsInfo.getHadoopConfDir(), "cancel job hadoopConfDir is required!");
        Preconditions.checkNotNull(
                jobParamsInfo.getApplicationId(), "cancel job applicationId is required!");
        Preconditions.checkNotNull(
                jobParamsInfo.getFlinkJobId(), "cancel job flinkJobId is required!");

        return new YarnPerJobClusterExecutor(jobParamsInfo).cancelJob(false);
    }

    @Override
    public FlinkResultInfo cancelFlinkJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo,
                FunctionUtils.uncheckedSupplier(() -> cancelFlinkJob(jobParamsInfo)));
    }

    @Override
    public FlinkResultInfo cancelFlinkJobDoSavepoint(FlinkParamsInfo jobParamsInfo) throws Exception {
        Preconditions.checkNotNull(
                jobParamsInfo.getHadoopConfDir(), "cancel job hadoopConfDir is required!");
        Preconditions.checkNotNull(
                jobParamsInfo.getApplicationId(), "cancel job applicationId is required!");
        Preconditions.checkNotNull(
                jobParamsInfo.getFlinkJobId(), "cancel job flinkJobId is required!");

        return new YarnPerJobClusterExecutor(jobParamsInfo).cancelJob(true);
    }

    @Override
    public FlinkResultInfo cancelFlinkJobDoSavepointWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo,
                FunctionUtils.uncheckedSupplier(() -> cancelFlinkJobDoSavepoint(jobParamsInfo)));
    }

    @Override
    public FlinkResultInfo savePointFlinkJob(FlinkParamsInfo jobParamsInfo) throws Exception {
        Preconditions.checkNotNull(
                jobParamsInfo.getHadoopConfDir(), "cancel job hadoopConfDir is required!");
        Preconditions.checkNotNull(
                jobParamsInfo.getApplicationId(), "cancel job applicationId is required!");
        Preconditions.checkNotNull(
                jobParamsInfo.getFlinkJobId(), "cancel job flinkJobId is required!");
        return new YarnPerJobClusterExecutor(jobParamsInfo).savePoint();
    }

    @Override
    public FlinkResultInfo savePointFlinkJobWithKerberos(FlinkParamsInfo jobParamsInfo) throws Exception {
        return KerberosSecurityContext.runSecured(
                jobParamsInfo,
                FunctionUtils.uncheckedSupplier(() -> savePointFlinkJob(jobParamsInfo)));
    }

    private static class YarnClientRemovalListener implements RemovalListener<String, YarnClient> {

        @Override
        public void onRemoval(RemovalNotification<String, YarnClient> clientCache) {
            logger.info(
                    "remove cache key={},value={},reason={},time:{}",
                    clientCache.getKey(),
                    clientCache.getValue(),
                    clientCache.getCause(),
                    LocalDateTime.now());
            Optional.ofNullable(clientCache.getValue()).ifPresent(YarnClient::stop);
        }
    }
}
