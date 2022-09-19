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

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.entity.FlinkResultInfo;
import org.apache.dolphinscheduler.plugin.task.flink.factory.YarnClusterDescriptorFactory;
import org.apache.dolphinscheduler.plugin.task.flink.utils.JobGraphBuildUtil;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.SecurityOptions;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.core.execution.SavepointFormatType;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.util.FlinkException;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnConfigOptionsInternal;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.apache.flink.util.Preconditions.checkNotNull;
import static org.apache.flink.yarn.configuration.YarnLogConfigUtil.CONFIG_FILE_LOG4J_NAME;
import static org.apache.flink.yarn.configuration.YarnLogConfigUtil.CONFIG_FILE_LOGBACK_NAME;

public abstract class AbstractClusterExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClusterExecutor.class);

    private static final String DEFAULT_TOTAL_PROCESS_MEMORY = "1024m";

    public FlinkParamsInfo jobParamsInfo;

    public AbstractClusterExecutor(FlinkParamsInfo jobParamsInfo) {
        this.jobParamsInfo = jobParamsInfo;
    }

    /**
     * submit job
     *
     * @return
     */
    public abstract FlinkResultInfo submitJob();

    public YarnClient createYarnClient() throws IOException {
        YarnClient yarnClient =
                YarnClusterDescriptorFactory.INSTANCE.createYarnClientFromHadoopConfDir(
                        jobParamsInfo.getHadoopConfDir());
        logger.info(
                "yarn client successfully created, hadoop conf dir:{}",
                jobParamsInfo.getHadoopConfDir());
        return yarnClient;
    }

    /**
     * kill yarn job and clean application files in hdfs.
     *
     * @return
     */
    public FlinkResultInfo killJob() throws IOException {
        String applicationId = jobParamsInfo.getApplicationId();
        if (StringUtils.isEmpty(applicationId)) {
            throw new NullPointerException("kill yarn job applicationId is required!");
        }
        logger.info("killed applicationId is:{}", applicationId);
        YarnConfiguration yarnConfiguration =
                YarnClusterDescriptorFactory.INSTANCE.parseYarnConfFromConfDir(
                        jobParamsInfo.getHadoopConfDir());

        try (
                YarnClient yarnClient =
                        YarnClusterDescriptorFactory.INSTANCE.createYarnClientFromYarnConf(
                                yarnConfiguration);) {
            yarnClient.killApplication(ApplicationId.fromString(applicationId));
            logger.info("killed applicationId {} was unsuccessful.", applicationId);
        } catch (YarnException e) {
            logger.error("killed applicationId {0} was failed.", e);
            return new FlinkResultInfo("", "");
        }

        try (FileSystem fs = FileSystem.get(yarnConfiguration)) {
            Path applicationDir =
                    new Path(
                            checkNotNull(fs.getHomeDirectory()),
                            ".flink/" + checkNotNull(applicationId) + '/');
            if (!fs.delete(applicationDir, true)) {
                logger.error(
                        "Deleting yarn application files under {} was unsuccessful.",
                        applicationDir);
            } else {
                logger.info(
                        "Deleting yarn application files under {} was successful.", applicationDir);
            }
        } catch (Exception e) {
            logger.error("Deleting yarn application files was failed!", e);
        }
        return new FlinkResultInfo("", "");
    }

    public FlinkResultInfo cancelJob(boolean doSavepoint) {
        String appId = jobParamsInfo.getApplicationId();
        String jobId = jobParamsInfo.getFlinkJobId();

        logger.info("cancel Job appId:{}, jobId:{}", appId, jobId);

        ApplicationId applicationId = ApplicationId.fromString(appId);
        JobID flinkJobId = new JobID(org.apache.flink.util.StringUtils.hexStringToByte(jobId));

        Configuration flinkConfig = getFlinkConfigFromParamsInfo();
        try (
                YarnClusterDescriptor clusterDescriptor =
                        (YarnClusterDescriptor) YarnClusterDescriptorFactory.INSTANCE.createClusterDescriptor(
                                jobParamsInfo.getHadoopConfDir(), flinkConfig)) {

            ClusterClientProvider<ApplicationId> retrieve =
                    clusterDescriptor.retrieve(applicationId);
            try (ClusterClient<ApplicationId> clusterClient = retrieve.getClusterClient()) {
                if (doSavepoint) {
                    CompletableFuture<String> savepointFuture =
                            clusterClient.cancelWithSavepoint(flinkJobId, null, SavepointFormatType.DEFAULT);
                    Object result = savepointFuture.get(2, TimeUnit.MINUTES);
                    logger.info("flink job savepoint path: {}", result.toString());
                } else {
                    CompletableFuture<Acknowledge> cancelFuture = clusterClient.cancel(flinkJobId);
                    Object result = cancelFuture.get(2, TimeUnit.MINUTES);
                    logger.info("flink job cancel result: {}", result.toString());
                }
            } catch (Exception e) {
                try {
                    logger.error("cancel job error, will kill job:", e);
                    clusterDescriptor.killCluster(applicationId);
                } catch (FlinkException e1) {
                    logger.error("yarn cluster Descriptor kill cluster error:", e);
                    return new FlinkResultInfo("", "");
                }
            }

        } catch (Exception e) {
            logger.error("cancel job failed,appId:{}, jobId:{}, exception:{}", appId, jobId, e);
            return new FlinkResultInfo(appId, jobId);
        }

        return new FlinkResultInfo(appId, jobId);
    }

    public FlinkResultInfo savePoint() {
        String appId = jobParamsInfo.getApplicationId();
        String jobId = jobParamsInfo.getFlinkJobId();

        logger.info("cancel Job appId:{}, jobId:{}", appId, jobId);

        ApplicationId applicationId = ApplicationId.fromString(appId);
        JobID flinkJobId = new JobID(org.apache.flink.util.StringUtils.hexStringToByte(jobId));

        Configuration flinkConfig = getFlinkConfigFromParamsInfo();
        try (
                YarnClusterDescriptor clusterDescriptor =
                        (YarnClusterDescriptor) YarnClusterDescriptorFactory.INSTANCE.createClusterDescriptor(
                                jobParamsInfo.getHadoopConfDir(), flinkConfig)) {

            ClusterClientProvider<ApplicationId> retrieve =
                    clusterDescriptor.retrieve(applicationId);
            try (ClusterClient<ApplicationId> clusterClient = retrieve.getClusterClient()) {
                CompletableFuture<String> savepointFuture =
                        clusterClient.triggerSavepoint(flinkJobId, null, SavepointFormatType.DEFAULT);
                Object result = savepointFuture.get(2, TimeUnit.MINUTES);
                logger.info("flink job savepoint path: {}", result.toString());
            } catch (Exception e) {
                logger.error("flink job savepoint error", e);
                throw new TaskException("flink job savepoint failed", e);
            }

        } catch (Exception e) {
            logger.error("flink job savepoint failed, appId:{}, jobId:{}, exception:{}", appId, jobId, e);
            throw new TaskException("flink job savepoint failed", e);
        }

        return new FlinkResultInfo(appId, jobId);
    }

    protected Configuration getFlinkConfigFromParamsInfo() {
        Configuration defaultGlobalConfig =
                JobGraphBuildUtil.getFlinkConfiguration(jobParamsInfo.getFlinkConfDir());
        replaceDefaultGlobalConfig(defaultGlobalConfig, jobParamsInfo);
        return defaultGlobalConfig;
    }

    /**
     * replace the default configuration items in the flink-conf.yaml
     *
     * @param flinkConfig
     * @param jobParamsInfo
     */
    protected void replaceDefaultGlobalConfig(Configuration flinkConfig, FlinkParamsInfo jobParamsInfo) {
        if (!StringUtils.isEmpty(jobParamsInfo.getName())) {
            flinkConfig.setString(YarnConfigOptions.APPLICATION_NAME, jobParamsInfo.getName());
        }

        if (!StringUtils.isEmpty(jobParamsInfo.getQueue())) {
            flinkConfig.setString(YarnConfigOptions.APPLICATION_QUEUE, jobParamsInfo.getQueue());
        }

        if (!StringUtils.isEmpty(jobParamsInfo.getFlinkConfDir())) {
            discoverLogConfigFile(jobParamsInfo.getFlinkConfDir())
                    .ifPresent(
                            file -> flinkConfig.setString(
                                    YarnConfigOptionsInternal.APPLICATION_LOG_CONFIG_FILE,
                                    file.getPath()));
        }

        if (!flinkConfig.contains(TaskManagerOptions.TOTAL_PROCESS_MEMORY)) {
            flinkConfig.setString(
                    TaskManagerOptions.TOTAL_PROCESS_MEMORY.key(), DEFAULT_TOTAL_PROCESS_MEMORY);
        }

        // fill security config
        if (jobParamsInfo.isOpenSecurity()) {
            flinkConfig.setString(
                    SecurityOptions.KERBEROS_LOGIN_KEYTAB.key(),
                    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH));
            Optional.ofNullable(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME))
                    .ifPresent(
                            principal -> flinkConfig.setString(
                                    SecurityOptions.KERBEROS_LOGIN_PRINCIPAL.key(),
                                    principal));
        }

        Properties flinkConfigProperties = jobParamsInfo.getConfProperties();
        if (!Objects.isNull(flinkConfigProperties)) {
            flinkConfigProperties.stringPropertyNames().stream()
                    .forEach(
                            key -> flinkConfig.setString(
                                    key, flinkConfigProperties.getProperty(key)));
        }
    }

    /**
     * find log4 files from flink conf
     *
     * @param configurationDirectory
     * @return
     */
    protected Optional<File> discoverLogConfigFile(final String configurationDirectory) {
        Optional<File> logConfigFile = Optional.empty();

        final File log4jFile =
                new File(configurationDirectory + File.separator + CONFIG_FILE_LOG4J_NAME);
        if (log4jFile.exists()) {
            logConfigFile = Optional.of(log4jFile);
        }

        final File logbackFile =
                new File(configurationDirectory + File.separator + CONFIG_FILE_LOGBACK_NAME);
        if (logbackFile.exists()) {
            if (logConfigFile.isPresent()) {
                logger.warn(
                        "The configuration directory ('"
                                + configurationDirectory
                                + "') already contains a logger4J config file. "
                                + "If you want to use logback, then please delete or rename the log configuration file.");
            } else {
                logConfigFile = Optional.of(logbackFile);
            }
        }
        return logConfigFile;
    }
}
