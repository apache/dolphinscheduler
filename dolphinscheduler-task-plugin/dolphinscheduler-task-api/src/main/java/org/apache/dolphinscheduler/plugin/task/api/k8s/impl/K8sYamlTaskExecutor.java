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

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.YamlUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.K8sYamlType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sOperation;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parser.TaskOutputParameterParser;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.dsl.LogWatch;

/**
 * K8sYamlTaskExecutor submits customized YAML k8s task to Kubernetes
 */
@Slf4j
public class K8sYamlTaskExecutor extends AbstractK8sTaskExecutor {

    // resource metadata parsed from user-customized YAML
    private HasMetadata metadata;

    // type of metadata, used to generate operation
    private K8sYamlType k8sYamlType;

    // k8s operation, generated based on `k8sYamlType`
    private AbstractK8sOperation abstractK8sOperation;

    protected boolean podLogOutputIsFinished = false;
    protected Future<?> podLogOutputFuture;

    // k8s pod label name to collect pod log
    public static final String DS_LOG_WATCH_LABEL_NAME = "ds-log-watch-label";

    public K8sYamlTaskExecutor(TaskExecutionContext taskRequest) {
        super(taskRequest);
    }

    /**
     * Executes a task based on the provided Kubernetes parameters.
     *
     * <p>This method processes the YAML content describing the Kubernetes job.</p>
     *
     * @param yamlContentString a string of user-customized YAML
     * @return a {@link TaskResponse} object containing the result of the task execution.
     * @throws Exception if an error occurs during task execution or while handling pod logs.
     */
    @Override
    public TaskResponse run(String yamlContentString) throws Exception {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();
        try {
            if (StringUtils.isEmpty(yamlContentString)) {
                return result;
            }

            K8sTaskExecutionContext k8sTaskExecutionContext = taskRequest.getK8sTaskExecutionContext();
            k8sUtils.buildClient(k8sTaskExecutionContext.getConfigYaml());

            // parse user-customized YAML string
            metadata = K8sUtils.getOrDefaultNamespacedResource(
                    YamlUtils.load(yamlContentString, new TypeReference<HasMetadata>() {
                    }));

            k8sYamlType = K8sYamlType.valueOf(this.metadata.getKind());
            generateOperation();

            submitJob2k8s(yamlContentString);
            parseLogOutput(metadata);
            registerBatchK8sYamlTaskWatcher(String.valueOf(taskInstanceId), result);

            if (podLogOutputFuture != null) {
                try {
                    // Wait kubernetes pod log collection finished
                    podLogOutputFuture.get();
                    log.info("[K8sYamlTaskExecutor-label-{}-{}] pod log collected successfully",
                            metadata.getMetadata().getName(), taskInstanceId);
                } catch (ExecutionException e) {
                    log.error("[K8sYamlTaskExecutor-label-{}-{}] Handle pod log error",
                            metadata.getMetadata().getName(), taskInstanceId, e);
                }
            }
        } catch (Exception e) {
            cancelApplication(yamlContentString);
            Thread.currentThread().interrupt();
            result.setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw e;
        }
        return result;
    }

    @Override
    public void cancelApplication(String yamlContentStr) {
        if (metadata != null) {
            stopJobOnK8s(yamlContentStr);
            final String taskName = metadata.getMetadata().getName();
            final int taskInstanceId = taskRequest.getTaskInstanceId();
            log.info("[K8sYamlTaskExecutor-label-{}-{}] K8s task canceled", taskName, taskInstanceId);
        }
    }

    @Override
    public void submitJob2k8s(String yamlContentString) {
        final String taskName = metadata.getMetadata().getName();
        final int taskInstanceId = taskRequest.getTaskInstanceId();
        try {
            abstractK8sOperation.createOrReplaceMetadata(metadata, taskInstanceId);
            log.info("[K8sYamlTaskExecutor-label-{}-{}] K8s task submitted successfully", taskName, taskInstanceId);
        } catch (Exception e) {
            log.error("[K8sYamlTaskExecutor-label-{}-{}] failed to submit job", taskName, taskInstanceId);
            e.printStackTrace();
            throw new TaskException("K8sYamlTaskExecutor failed to submit job", e);
        }
    }

    @Override
    public void stopJobOnK8s(String k8sParameterStr) {
        try {
            abstractK8sOperation.stopMetadata(this.metadata);
        } catch (Exception e) {
            String taskName = this.metadata.getMetadata().getName();
            String taskNamespace = this.metadata.getMetadata().getNamespace();
            log.error("[K8sYamlTaskExecutor-label-{}] fail to stop job in namespace {}", taskName, taskNamespace);
            throw new TaskException("K8sYamlTaskExecutor fail to stop job", e);
        }
    }

    /**
     * Generates the Kubernetes operation based on the Kubernetes YAML type.
     */
    private void generateOperation() {
        switch (k8sYamlType) {
            case Pod:
                abstractK8sOperation = new K8sPodOperation(k8sUtils.getClient());
                break;
            default:
                throw new TaskException(
                        String.format("K8sYamlTaskExecutor do not support type %s", k8sYamlType.name()));
        }
    }

    public void registerBatchK8sYamlTaskWatcher(String taskInstanceId, TaskResponse taskResponse) {
        final String taskName = metadata.getMetadata().getName();
        final String taskNamespace = metadata.getMetadata().getNamespace();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        try (
                Watch watch =
                        abstractK8sOperation.createBatchWatcher(countDownLatch, taskResponse, metadata, taskRequest)) {
            boolean timeoutFlag = taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                    || taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
            if (timeoutFlag) {
                Boolean timeout = !(countDownLatch.await(taskRequest.getTaskTimeout(), TimeUnit.SECONDS));
                waitTimeout(timeout);
            } else {
                countDownLatch.await();
            }
        } catch (InterruptedException e) {
            log.error("[K8sYamlTaskExecutor-label-{}-{}] failed in namespace `{}`: {}",
                    taskName, taskInstanceId, taskNamespace, e.getMessage(), e);
            Thread.currentThread().interrupt();
            taskResponse.setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
        } catch (Exception e) {
            log.error("[K8sYamlTaskExecutor-label-{}-{}] failed in namespace `{}`: {}",
                    taskName, taskInstanceId, taskNamespace, e.getMessage(), e);
            e.printStackTrace();
            taskResponse.setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
        }
    }

    private void parseLogOutput(HasMetadata resource) {
        ObjectMeta resourceMetadata = resource.getMetadata();
        final int taskInstanceId = taskRequest.getTaskInstanceId();
        final int workflowInstanceId = taskRequest.getProcessInstanceId();
        final String taskName = resourceMetadata.getName().toLowerCase(Locale.ROOT);
        final String namespace = resourceMetadata.getNamespace();
        final String labelPodLogWatch = String.format("%s-%d", taskName, taskInstanceId);

        ExecutorService collectPodLogExecutorService = ThreadUtils
                .newSingleDaemonScheduledExecutorService("CollectPodLogOutput-thread-" + taskName);

        podLogOutputFuture = collectPodLogExecutorService.submit(() -> {
            TaskOutputParameterParser taskOutputParameterParser = new TaskOutputParameterParser();
            LogUtils.setWorkflowAndTaskInstanceIDMDC(workflowInstanceId, taskInstanceId);
            LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());

            try (LogWatch watcher = abstractK8sOperation.getLogWatcher(labelPodLogWatch, namespace)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(watcher.getOutput()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[k8s-label-{}-pod-log] {}", labelPodLogWatch, line);
                        taskOutputParameterParser.appendParseLog(line);
                    }
                } catch (Exception e) {
                    log.error("[k8s-label-{}-pod-log] failed to open BufferedReader on LogWatch", labelPodLogWatch);
                    e.printStackTrace();
                    throw new RuntimeException("failed to open LogWatch", e);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                LogUtils.removeTaskInstanceLogFullPathMDC();
                podLogOutputIsFinished = true;
            }
            taskOutputParams = taskOutputParameterParser.getTaskOutputParams();
            log.info("[k8s-label-{}-result] ----------BEGIN K8S POD RESULT----------", labelPodLogWatch);
            for (Map.Entry<String, String> entry : taskOutputParams.entrySet()) {
                log.info("[k8s-label-{}-result] (key, value) = ('{}', '{}')",
                        labelPodLogWatch, entry.getKey(), entry.getValue());
            }
            log.info("[k8s-label-{}-result] ----------END K8S POD RESULT----------", labelPodLogWatch);
        });

        collectPodLogExecutorService.shutdown();
    }
}
