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
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.IMAGE_PULL_POLICY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JOB_TTL_SECONDS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LAYER_LABEL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LAYER_LABEL_VALUE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MEMORY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.MI;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.NAME_LABEL;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RESTART_POLICY;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_INSTANCE_ID;

import org.apache.dolphinscheduler.plugin.task.api.K8sTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContextCacheManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.k8s.AbstractK8sTaskExecutor;
import org.apache.dolphinscheduler.plugin.task.api.k8s.K8sTaskMainParameters;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.MapUtils;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;

/**
 * K8sTaskExecutor used to submit k8s task to K8S
 */
public class K8sTaskExecutor extends AbstractK8sTaskExecutor {
    private Job job;
    public K8sTaskExecutor(Logger logger, TaskExecutionContext taskRequest) {
        super(logger,taskRequest);
    }

    public Job buildK8sJob(K8sTaskMainParameters k8STaskMainParameters) {
        String taskInstanceId = String.valueOf(taskRequest.getTaskInstanceId());
        String taskName = taskRequest.getTaskName().toLowerCase(Locale.ROOT);
        String image = k8STaskMainParameters.getImage();
        String namespaceName = k8STaskMainParameters.getNamespaceName();
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
        Map<String, String> labelMap = new HashMap<>();
        labelMap.put(LAYER_LABEL, LAYER_LABEL_VALUE);
        labelMap.put(NAME_LABEL, k8sJobName);
        EnvVar taskInstanceIdVar = new EnvVar(TASK_INSTANCE_ID, taskInstanceId, null);
        List<EnvVar> envVars = new ArrayList<>();
        envVars.add(taskInstanceIdVar);
        if (MapUtils.isNotEmpty(otherParams)) {
            for (Map.Entry<String,String> entry : otherParams.entrySet()) {
                String param = entry.getKey();
                String paramValue = entry.getValue();
                EnvVar envVar = new EnvVar(param, paramValue, null);
                envVars.add(envVar);
            }
        }
        return new JobBuilder()
            .withApiVersion(API_VERSION)
            .withNewMetadata()
            .withName(k8sJobName)
            .withLabels(labelMap)
            .withNamespace(namespaceName)
            .endMetadata()
            .withNewSpec()
            .withTtlSecondsAfterFinished(JOB_TTL_SECONDS)
            .withNewTemplate()
            .withNewSpec()
            .addNewContainer()
            .withName(k8sJobName)
            .withImage(image)
            .withImagePullPolicy(IMAGE_PULL_POLICY)
            .withResources(new ResourceRequirements(limitRes, reqRes))
            .withEnv(envVars)
            .endContainer()
            .withRestartPolicy(RESTART_POLICY)
            .endSpec()
            .endTemplate()
            .withBackoffLimit(retryNum)
            .endSpec()
            .build();
    }

    public void registerBatchJobWatcher(Job job, String taskInstanceId, TaskResponse taskResponse, K8sTaskMainParameters k8STaskMainParameters) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Watcher<Job> watcher = new Watcher<Job>() {
            @Override
            public void eventReceived(Action action, Job job) {
                if (action != Action.ADDED) {
                    int jobStatus = getK8sJobStatus(job);
                    setTaskStatus(jobStatus,taskInstanceId, taskResponse, k8STaskMainParameters);
                    countDownLatch.countDown();
                    }
                }

            @Override
            public void onClose(WatcherException e) {
                logStringBuffer.append(String.format("[K8sJobExecutor-%s] fail in k8s: %s",job.getMetadata().getName(),e.getMessage()));
                taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
                countDownLatch.countDown();
            }

            @Override
            public void onClose() {
                logger.warn("Watch gracefully closed");
            }
        };
        Watch watch = null;
        try {
            watch = k8sUtils.createBatchJobWatcher(job.getMetadata().getName(), watcher);
            boolean timeoutFlag = taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                || taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
            if (timeoutFlag) {
                Boolean timeout = !(countDownLatch.await(taskRequest.getTaskTimeout(), TimeUnit.SECONDS));
                waitTimeout(timeout);
            } else {
                countDownLatch.await();
            }
            flushLog(taskResponse);
        } catch (InterruptedException e) {
            logger.error("job failed in k8s: {}",e.getMessage(), e);
            Thread.currentThread().interrupt();
            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
        } catch (Exception e) {
            logger.error("job failed in k8s: {}",e.getMessage(), e);
            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
        } finally {
            if (watch != null) {
                watch.close();
            }
        }
    }

    @Override
    public TaskResponse run(String k8sParameterStr) throws Exception {
        TaskResponse result = new TaskResponse();
        int taskInstanceId = taskRequest.getTaskInstanceId();
        K8sTaskMainParameters k8STaskMainParameters = JSONUtils.parseObject(k8sParameterStr, K8sTaskMainParameters.class);
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
            String  configYaml = k8sTaskExecutionContext.getConfigYaml();
            k8sUtils.buildClient(configYaml);
            submitJob2k8s(k8sParameterStr);
            registerBatchJobWatcher(job, Integer.toString(taskInstanceId), result, k8STaskMainParameters);
        } catch (Exception e) {
            cancelApplication(k8sParameterStr);
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
        K8sTaskMainParameters k8STaskMainParameters = JSONUtils.parseObject(k8sParameterStr, K8sTaskMainParameters.class);
        try {
            logger.info("[K8sJobExecutor-{}-{}] start to submit job", taskName, taskInstanceId);
            job = buildK8sJob(k8STaskMainParameters);
            stopJobOnK8s(k8sParameterStr);
            String namespaceName = k8STaskMainParameters.getNamespaceName();
            k8sUtils.createJob(namespaceName, job);
            logger.info("[K8sJobExecutor-{}-{}]  submitted job successfully", taskName, taskInstanceId);
        } catch (Exception e) {
            logger.error("[K8sJobExecutor-{}-{}]  fail to submit job", taskName, taskInstanceId);
            throw new TaskException("K8sJobExecutor fail to submit job", e);
        }
    }

    @Override
    public void stopJobOnK8s(String k8sParameterStr) {
        K8sTaskMainParameters k8STaskMainParameters = JSONUtils.parseObject(k8sParameterStr, K8sTaskMainParameters.class);
        String namespaceName = k8STaskMainParameters.getNamespaceName();
        String jobName = job.getMetadata().getName();
        try {
            if (Boolean.TRUE.equals(k8sUtils.jobExist(jobName, namespaceName))) {
                k8sUtils.deleteJob(jobName, namespaceName);
            }
        } catch (Exception e) {
            logger.error("[K8sJobExecutor-{}]  fail to stop job", jobName);
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

    public void setTaskStatus(int jobStatus,String taskInstanceId, TaskResponse taskResponse, K8sTaskMainParameters k8STaskMainParameters) {
        if (jobStatus == EXIT_CODE_SUCCESS || jobStatus == EXIT_CODE_FAILURE) {
            if (null == TaskExecutionContextCacheManager.getByTaskInstanceId(Integer.valueOf(taskInstanceId))) {
                logStringBuffer.append(String.format("[K8sJobExecutor-%s] killed", job.getMetadata().getName()));
                taskResponse.setExitStatusCode(EXIT_CODE_KILL);
            } else if (jobStatus == EXIT_CODE_SUCCESS) {
                logStringBuffer.append(String.format("[K8sJobExecutor-%s] succeed in k8s", job.getMetadata().getName()));
                taskResponse.setExitStatusCode(EXIT_CODE_SUCCESS);
            } else {
                String errorMessage = k8sUtils.getPodLog(job.getMetadata().getName(), k8STaskMainParameters.getNamespaceName());
                logStringBuffer.append(String.format("[K8sJobExecutor-%s] fail in k8s: %s", job.getMetadata().getName(), errorMessage));
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
