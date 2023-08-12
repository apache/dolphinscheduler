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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.API_VERSION;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.CPU;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_KILL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JOB_TTL_SECONDS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LAYER_LABEL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LAYER_LABEL_VALUE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MEMORY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MI;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAME_LABEL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RESTART_POLICY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_INSTANCE_ID;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.UNIQUE_LABEL_NAME;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.k8s.K8sTaskMainParameters;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.ProcessUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import io.fabric8.kubernetes.api.model.Affinity;
import io.fabric8.kubernetes.api.model.AffinityBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.NodeSelectorTerm;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.LogWatch;

/**
 * K8sTaskExecutor used to submit k8s task to K8S
 */
public class K8sTaskExecutor extends AbstractK8sTaskExecutor {

    private Job job;
    protected boolean podLogOutputIsFinished = false;
    protected Future<?> podLogOutputFuture;

    public K8sTaskExecutor(Logger logger, TaskExecutionContext taskRequest) {
        super(logger, taskRequest);
    }

    public Job buildK8sJob(K8sTaskMainParameters k8STaskMainParameters) {
        String taskInstanceId = String.valueOf(taskRequest.getTaskInstanceId());
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        String image = k8STaskMainParameters.getImage();
        String namespaceName = k8STaskMainParameters.getNamespaceName();
        String imagePullPolicy = k8STaskMainParameters.getImagePullPolicy();
        Map<String, String> otherParams = k8STaskMainParameters.getParamsMap();
        Double podMem = k8STaskMainParameters.getMinMemorySpace();
        Double podCpu = k8STaskMainParameters.getMinCpuCores();
        Double limitPodMem = podMem * 2;
        Double limitPodCpu = podCpu * 2;
        int retryNum = 0;
        String k8sJobName = String.format("%s-%s", taskName, taskInstanceId);
        Map<String, Quantity> reqRes = new HashMap<>();
        reqRes.put(MEMORY, new Quantity(String.format("%s%s", podMem, MI)));
        reqRes.put(CPU, new Quantity(String.valueOf(podCpu)));
        Map<String, Quantity> limitRes = new HashMap<>();
        limitRes.put(MEMORY, new Quantity(String.format("%s%s", limitPodMem, MI)));
        limitRes.put(CPU, new Quantity(String.valueOf(limitPodCpu)));
        Map<String, String> labelMap = k8STaskMainParameters.getLabelMap();
        labelMap.put(LAYER_LABEL, LAYER_LABEL_VALUE);
        labelMap.put(NAME_LABEL, k8sJobName);
        Map<String, String> podLabelMap = new HashMap<>();
        podLabelMap.put(UNIQUE_LABEL_NAME, taskRequest.getTaskAppId());
        EnvVar taskInstanceIdVar = new EnvVar(TASK_INSTANCE_ID, taskInstanceId, null);
        List<EnvVar> envVars = new ArrayList<>();
        envVars.add(taskInstanceIdVar);
        if (MapUtils.isNotEmpty(otherParams)) {
            for (Map.Entry<String, String> entry : otherParams.entrySet()) {
                String param = entry.getKey();
                String paramValue = entry.getValue();
                EnvVar envVar = new EnvVar(param, paramValue, null);
                envVars.add(envVar);
            }
        }

        String commandString = k8STaskMainParameters.getCommand();
        String argsString = k8STaskMainParameters.getArgs();
        List<String> commands = new ArrayList<>();
        List<String> args = new ArrayList<>();

        try {
            if (!StringUtils.isEmpty(commandString)) {
                commands = yaml.load(commandString.trim());
            }
            if (!StringUtils.isEmpty(argsString)) {
                args = yaml.load(argsString.trim());
            }
        } catch (Exception e) {
            throw new TaskException("Parse yaml-like commands and args failed", e);
        }

        NodeSelectorTerm nodeSelectorTerm = new NodeSelectorTerm();
        nodeSelectorTerm.setMatchExpressions(k8STaskMainParameters.getNodeSelectorRequirements());

        Affinity affinity = k8STaskMainParameters.getNodeSelectorRequirements().size() == 0 ? null
                : new AffinityBuilder()
                        .withNewNodeAffinity()
                        .withNewRequiredDuringSchedulingIgnoredDuringExecution()
                        .addNewNodeSelectorTermLike(nodeSelectorTerm)
                        .endNodeSelectorTerm()
                        .endRequiredDuringSchedulingIgnoredDuringExecution()
                        .endNodeAffinity().build();

        JobBuilder jobBuilder = new JobBuilder()
                .withApiVersion(API_VERSION)
                .withNewMetadata()
                .withName(k8sJobName)
                .withLabels(labelMap)
                .withNamespace(namespaceName)
                .endMetadata()
                .withNewSpec()
                .withTtlSecondsAfterFinished(JOB_TTL_SECONDS)
                .withNewTemplate()
                .withNewMetadata()
                .withLabels(podLabelMap)
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName(k8sJobName)
                .withImage(image)
                .withCommand(commands.size() == 0 ? null : commands)
                .withArgs(args.size() == 0 ? null : args)
                .withImagePullPolicy(imagePullPolicy)
                .withResources(new ResourceRequirements(limitRes, reqRes))
                .withEnv(envVars)
                .endContainer()
                .withRestartPolicy(RESTART_POLICY)
                .withAffinity(affinity)
                .endSpec()
                .endTemplate()
                .withBackoffLimit(retryNum)
                .endSpec();

        return jobBuilder.build();
    }

    public void registerBatchJobWatcher(Job job, String taskInstanceId, TaskResponse taskResponse) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Watcher<Job> watcher = new Watcher<Job>() {

            @Override
            public void eventReceived(Action action, Job job) {
                try (
                        final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                                LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath())) {
                    log.info("event received : job:{} action:{}", job.getMetadata().getName(), action);
                    if (action == Action.DELETED) {
                        log.error("[K8sJobExecutor-{}] fail in k8s", job.getMetadata().getName());
                        taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
                        countDownLatch.countDown();
                    } else if (action != Action.ADDED) {
                        int jobStatus = getK8sJobStatus(job);
                        log.info("job {} status {}", job.getMetadata().getName(), jobStatus);
                        if (jobStatus == TaskConstants.RUNNING_CODE) {
                            return;
                        }
                        setTaskStatus(jobStatus, taskInstanceId, taskResponse);
                        countDownLatch.countDown();
                    }
                }
            }

            @Override
            public void onClose(WatcherException e) {
                log.error("[K8sJobExecutor-{}] fail in k8s: {}", job.getMetadata().getName(), e.getMessage());
                taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
                countDownLatch.countDown();
            }
        };
        try (Watch watch = k8sUtils.createBatchJobWatcher(job.getMetadata().getName(), watcher)) {
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

    private void parsePodLogOutput() {
        ExecutorService collectPodLogExecutorService = ThreadUtils
                .newSingleDaemonScheduledExecutorService("CollectPodLogOutput-thread-" + taskRequest.getTaskName());

        String taskInstanceId = String.valueOf(taskRequest.getTaskInstanceId());
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        String containerName = String.format("%s-%s", taskName, taskInstanceId);
        podLogOutputFuture = collectPodLogExecutorService.submit(() -> {
            try (
                    final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());
                    LogWatch watcher = ProcessUtils.getPodLogWatcher(taskRequest.getK8sTaskExecutionContext(),
                            taskRequest.getTaskAppId(), containerName)) {
                String line;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(watcher.getOutput()))) {
                    while ((line = reader.readLine()) != null) {
                        log.info("[K8S-pod-log] {}", line);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                podLogOutputIsFinished = true;
            }
        });

        collectPodLogExecutorService.shutdown();
    }

    @Override
    public TaskResponse run(String k8sParameterStr) throws Exception {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();
        try {
            if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(taskInstanceId)) {
                result.setExitStatusCode(EXIT_CODE_KILL);
                return result;
            }
            if (StringUtils.isEmpty(k8sParameterStr)) {
                TaskExecutionContextCacheManager.removeByTaskInstanceId(taskInstanceId);
                return result;
            }
            K8sTaskExecutionContext k8sTaskExecutionContext = taskRequest.getK8sTaskExecutionContext();
            String configYaml = k8sTaskExecutionContext.getConfigYaml();
            k8sUtils.buildClient(configYaml);
            submitJob2k8s(k8sParameterStr);
            parsePodLogOutput();
            registerBatchJobWatcher(job, Integer.toString(taskInstanceId), result);

            if (podLogOutputFuture != null) {
                try {
                    // Wait kubernetes pod log collection finished
                    podLogOutputFuture.get();
                } catch (ExecutionException e) {
                    log.error("Handle pod log error", e);
                }
            }
        } catch (Exception e) {
            cancelApplication(k8sParameterStr);
            Thread.currentThread().interrupt();
            result.setExitStatusCode(EXIT_CODE_FAILURE);
            throw e;
        }
        return result;
    }

    @Override
    public void cancelApplication(String k8sParameterStr) {
        if (job != null) {
            stopJobOnK8s(k8sParameterStr);
        }
    }

    @Override
    public void submitJob2k8s(String k8sParameterStr) {
        int taskInstanceId = taskRequest.getTaskInstanceId();
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        K8sTaskMainParameters k8STaskMainParameters =
                JSONUtils.parseObject(k8sParameterStr, K8sTaskMainParameters.class);
        try {
            log.info("[K8sJobExecutor-{}-{}] start to submit job", taskName, taskInstanceId);
            job = buildK8sJob(k8STaskMainParameters);
            stopJobOnK8s(k8sParameterStr);
            String namespaceName = k8STaskMainParameters.getNamespaceName();
            k8sUtils.createJob(namespaceName, job);
            log.info("[K8sJobExecutor-{}-{}] submitted job successfully", taskName, taskInstanceId);
        } catch (Exception e) {
            log.error("[K8sJobExecutor-{}-{}] fail to submit job", taskName, taskInstanceId);
            throw new TaskException("K8sJobExecutor fail to submit job", e);
        }
    }

    @Override
    public void stopJobOnK8s(String k8sParameterStr) {
        K8sTaskMainParameters k8STaskMainParameters =
                JSONUtils.parseObject(k8sParameterStr, K8sTaskMainParameters.class);
        String namespaceName = k8STaskMainParameters.getNamespaceName();
        String jobName = job.getMetadata().getName();
        try {
            if (Boolean.TRUE.equals(k8sUtils.jobExist(jobName, namespaceName))) {
                k8sUtils.deleteJob(jobName, namespaceName);
            }
        } catch (Exception e) {
            log.error("[K8sJobExecutor-{}] fail to stop job", jobName);
            throw new TaskException("K8sJobExecutor fail to stop job", e);
        }
    }

    public int getK8sJobStatus(Job job) {
        JobStatus jobStatus = job.getStatus();
        if (jobStatus.getSucceeded() != null && jobStatus.getSucceeded() == 1) {
            return EXIT_CODE_SUCCESS;
        } else if (jobStatus.getFailed() != null && jobStatus.getFailed() == 1) {
            return EXIT_CODE_FAILURE;
        } else {
            return TaskConstants.RUNNING_CODE;
        }
    }

    public void setTaskStatus(int jobStatus, String taskInstanceId, TaskResponse taskResponse) {
        if (jobStatus == EXIT_CODE_SUCCESS || jobStatus == EXIT_CODE_FAILURE) {
            if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(Integer.valueOf(taskInstanceId))) {
                log.info("[K8sJobExecutor-{}] killed", job.getMetadata().getName());
                taskResponse.setExitStatusCode(EXIT_CODE_KILL);
            } else if (jobStatus == EXIT_CODE_SUCCESS) {
                log.info("[K8sJobExecutor-{}] succeed in k8s", job.getMetadata().getName());
                taskResponse.setExitStatusCode(EXIT_CODE_SUCCESS);
            } else {
                log.error("[K8sJobExecutor-{}] fail in k8s", job.getMetadata().getName());
                taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
            }
        }
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }
}
