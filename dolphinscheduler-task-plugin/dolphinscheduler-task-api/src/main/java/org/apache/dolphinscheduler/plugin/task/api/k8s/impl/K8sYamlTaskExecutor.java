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

package org.apache.dolphinscheduler.plugin.task.api.k8s.impl;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.*;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sOperation;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.k8s.K8sTaskMainParameters;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.YamlContent;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.VarPoolUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;

import static java.util.Collections.singletonList;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.*;

/**
 * K8sTaskExecutor used to submit k8s task to K8S
 */
@Slf4j
public class K8sYamlTaskExecutor extends AbstractK8sTaskExecutor {

    private HasMetadata metadata;
    protected boolean podLogOutputIsFinished = false;
    protected Future<?> podLogOutputFuture;
    private YamlType yamlType;
    private AbstractK8sOperation abstractK8sOperation;

    public K8sYamlTaskExecutor(TaskExecutionContext taskRequest) {
        super(log,taskRequest);
    }


    public void registerBatchWatcher(String taskInstanceId, TaskResponse taskResponse) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try (Watch watch = abstractK8sOperation.createBatchWatcher(countDownLatch,taskResponse,metadata,taskRequest)) {
            boolean timeoutFlag = taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                    || taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
            if (timeoutFlag) {
                Boolean timeout = !(countDownLatch.await(taskRequest.getTaskTimeout(), TimeUnit.SECONDS));
                waitTimeout(timeout);
            } else {
                countDownLatch.await();
            }
        } catch (InterruptedException e) {
            log.error("job failed in k8s: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
        } catch (Exception e) {
            log.error("job failed in k8s: {}", e.getMessage(), e);
            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
        }
    }

    private void parseLogOutput() {
        ExecutorService collectPodLogExecutorService = ThreadUtils
                .newSingleDaemonScheduledExecutorService("CollectPodLogOutput-thread-" + taskRequest.getTaskName());

        String taskInstanceId = String.valueOf(taskRequest.getTaskInstanceId());
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        String containerName = String.format("%s-%s", taskName, taskInstanceId);
        podLogOutputFuture = collectPodLogExecutorService.submit(() -> {
            try (
                    LogWatch watcher = abstractK8sOperation.getLogWatcher(containerName,metadata.getMetadata().getNamespace())) {
                LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(watcher.getOutput()))) {
                    while ((line = reader.readLine()) != null) {
                        log.info("[K8S-pod-log] {}", line);

                        if (line.endsWith(VarPoolUtils.VAR_SUFFIX)) {
                            varPool.append(VarPoolUtils.findVarPool(line));
                            varPool.append(VarPoolUtils.VAR_DELIMITER);
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                LogUtils.removeTaskInstanceLogFullPathMDC();
                podLogOutputIsFinished = true;
            }
        });

        collectPodLogExecutorService.shutdown();
    }

    @Override
    public TaskResponse run(String yamlContentString) throws Exception {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();

        try {
            if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId)) {
                result.setExitStatusCode(EXIT_CODE_KILL);
                return result;
            }
            if (StringUtils.isEmpty(yamlContentString)) {
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
                return result;
            }
            YamlContent yamlContent = JSONUtils.parseObject(yamlContentString, YamlContent.class);
            yamlType = yamlContent.getType();
            K8sTaskExecutionContext k8sTaskExecutionContext = taskRequest.getK8sTaskExecutionContext();
            k8sUtils.buildClient(k8sTaskExecutionContext.getConfigYaml());
            generateOperation();
            submitJob2k8s(yamlContentString);
            parseLogOutput();

            registerBatchWatcher(Integer.toString(taskInstanceId), result);

            if (podLogOutputFuture != null) {
                try {
                    // Wait kubernetes pod log collection finished
                    podLogOutputFuture.get();
                } catch (ExecutionException e) {
                    log.error("Handle pod log error", e);
                }
            }
        } catch (Exception e) {
            cancelApplication(yamlContentString);
            Thread.currentThread().interrupt();
            result.setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }
        return result;
    }


    @Override
    public void cancelApplication(String k8sParameterStr) {
        if (metadata != null) {
            stopJobOnK8s(k8sParameterStr);
        }
    }

    @Override
    public void submitJob2k8s(String yamlContentString) {
        int taskInstanceId = taskRequest.getTaskInstanceId();
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        YamlContent yamlContent = JSONUtils.parseObject(yamlContentString, YamlContent.class);
        metadata = abstractK8sOperation.buildMetadata(yamlContent);
        Map<String, String> labelMap = metadata.getMetadata().getLabels();
        String k8sJobName = String.format("%s-%s", taskName, taskInstanceId);
        if (MapUtils.isEmpty(labelMap)) {
            labelMap = new HashMap<String, String>(1);
        }
        // add special label which make people to get it simple
        labelMap.put(LAYER_LABEL, LAYER_LABEL_VALUE);
        labelMap.put(NAME_LABEL, k8sJobName);
        try {
            log.info("[K8sYamlJobExecutor-{}-{}] start to submit job", taskName, taskInstanceId);
            abstractK8sOperation.createOrReplaceMetadata(metadata);
            log.info("[K8sYamlJobExecutor-{}-{}] submitted job successfully", taskName, taskInstanceId);
        } catch (Exception e) {
            log.error("[K8sYamlJobExecutor-{}-{}] fail to submit job", taskName, taskInstanceId);
            throw new TaskException("K8sYamlJobExecutor fail to submit job", e);
        }
    }


    @Override
    public void stopJobOnK8s(String k8sParameterStr) {
        K8sTaskMainParameters k8STaskMainParameters =
                JSONUtils.parseObject(k8sParameterStr, K8sTaskMainParameters.class);
        String namespaceName = k8STaskMainParameters.getNamespaceName();
        String jobName = metadata.getMetadata().getName();
        try {
            if (Boolean.TRUE.equals(k8sUtils.jobExist(jobName, namespaceName))) {
                k8sUtils.deleteJob(jobName, namespaceName);
            }
        } catch (Exception e) {
            log.error("[K8sYamlJobExecutor-{}] fail to stop job", jobName);
            throw new TaskException("K8sYamlJobExecutor fail to stop job", e);
        }
    }


    void generateOperation(){
        switch (yamlType){
            case POD:
                abstractK8sOperation = new PodOperation(k8sUtils.getClient());
            case DEPLOYMENT:
                abstractK8sOperation = new DeploymentOperation(k8sUtils.getClient());
            case SERVICE:
                abstractK8sOperation = new ServiceOperation(k8sUtils.getClient());
            case CONFIGMAP:
                abstractK8sOperation = new ConfigMapOperation(k8sUtils.getClient());
            default:
                throw new TaskException(String.format("do not support type {}",yamlType));
        }
    }
}
