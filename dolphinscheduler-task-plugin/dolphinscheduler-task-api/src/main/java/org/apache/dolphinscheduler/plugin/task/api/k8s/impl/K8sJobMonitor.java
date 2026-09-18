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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.utils.K8sUtils;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobStatus;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;

/**
 * Monitors a submitted Kubernetes Batch Job until it reaches a terminal state.
 * The GET polling path is a bounded safety net when informer events are missed or unavailable.
 */
@Slf4j
public class K8sJobMonitor {

    static final long DEFAULT_POLL_INTERVAL_SECONDS = 30L;
    static final int DEFAULT_MAX_CONSECUTIVE_POLL_FAILURES = 3;

    private final K8sUtils k8sUtils;
    private final TaskExecutionContext taskRequest;
    private final long pollIntervalSeconds;
    private final int maxConsecutivePollFailures;

    public K8sJobMonitor(K8sUtils k8sUtils, TaskExecutionContext taskRequest) {
        this(k8sUtils, taskRequest, DEFAULT_POLL_INTERVAL_SECONDS, DEFAULT_MAX_CONSECUTIVE_POLL_FAILURES);
    }

    K8sJobMonitor(K8sUtils k8sUtils,
                  TaskExecutionContext taskRequest,
                  long pollIntervalSeconds,
                  int maxConsecutivePollFailures) {
        this.k8sUtils = k8sUtils;
        this.taskRequest = taskRequest;
        this.pollIntervalSeconds = pollIntervalSeconds;
        this.maxConsecutivePollFailures = maxConsecutivePollFailures;
    }

    public void monitorUntilTerminal(Job job, TaskResponse taskResponse) {
        final String jobName = job.getMetadata().getName();
        final String namespace = job.getMetadata().getNamespace();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final AtomicBoolean completed = new AtomicBoolean(false);
        SharedIndexInformer<Job> informer = null;
        ScheduledExecutorService jobStatusPoller = null;
        try {
            informer = k8sUtils.createBatchJobInformer(jobName, namespace,
                    createBatchJobEventHandler(taskResponse, countDownLatch, completed));
            informer.start().whenComplete((v, ex) -> {
                if (ex != null && !completed.get()) {
                    withTaskLogContext(() -> {
                        log.error("[K8sJobMonitor-{}] fail in k8s: {}", jobName, ex.getMessage(), ex);
                        completeOnce(completed, countDownLatch, taskResponse, EXIT_CODE_FAILURE, jobName);
                    });
                }
            });
            AtomicInteger consecutivePollFailures = new AtomicInteger(0);
            jobStatusPoller = startJobStatusPoller(jobName, namespace, taskResponse, countDownLatch, completed,
                    consecutivePollFailures);
            awaitJobCompletion(countDownLatch);
        } catch (InterruptedException e) {
            log.error("job failed in k8s: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
        } catch (Exception e) {
            log.error("job failed in k8s: {}", e.getMessage(), e);
            taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
        } finally {
            if (jobStatusPoller != null) {
                jobStatusPoller.shutdownNow();
            }
            if (informer != null) {
                informer.stop();
            }
        }
    }

    private ScheduledExecutorService startJobStatusPoller(String jobName,
                                                          String namespace,
                                                          TaskResponse taskResponse,
                                                          CountDownLatch countDownLatch,
                                                          AtomicBoolean completed,
                                                          AtomicInteger consecutivePollFailures) {
        ScheduledExecutorService jobStatusPoller = ThreadUtils.newSingleDaemonScheduledExecutorService(
                "K8sJobStatusPoller-" + jobName);
        jobStatusPoller.scheduleAtFixedRate(
                () -> pollBatchJobStatus(jobName, namespace, taskResponse, countDownLatch, completed,
                        consecutivePollFailures),
                pollIntervalSeconds,
                pollIntervalSeconds,
                TimeUnit.SECONDS);
        return jobStatusPoller;
    }

    private void pollBatchJobStatus(String jobName,
                                    String namespace,
                                    TaskResponse taskResponse,
                                    CountDownLatch countDownLatch,
                                    AtomicBoolean completed,
                                    AtomicInteger consecutivePollFailures) {
        if (completed.get()) {
            return;
        }
        withTaskLogContext(() -> {
            try {
                log.info("[K8sJobMonitor-{}] polling job status via GET", jobName);
                Job polledJob = k8sUtils.getJob(jobName, namespace);
                consecutivePollFailures.set(0);
                if (polledJob == null) {
                    log.error("[K8sJobMonitor-{}] job not found during status polling", jobName);
                    completeOnce(completed, countDownLatch, taskResponse, EXIT_CODE_FAILURE, jobName);
                    return;
                }
                handleBatchJobTerminalStatus(polledJob, taskResponse, countDownLatch, completed);
            } catch (Exception e) {
                int failures = consecutivePollFailures.incrementAndGet();
                log.warn("[K8sJobMonitor-{}] failed to poll job status ({}/{}): {}",
                        jobName, failures, maxConsecutivePollFailures, e.getMessage());
                if (failures >= maxConsecutivePollFailures) {
                    log.error("[K8sJobMonitor-{}] fail in k8s: exceeded consecutive poll failure limit", jobName);
                    completeOnce(completed, countDownLatch, taskResponse, EXIT_CODE_FAILURE, jobName);
                }
            }
        });
    }

    private ResourceEventHandler<Job> createBatchJobEventHandler(TaskResponse taskResponse,
                                                                 CountDownLatch countDownLatch,
                                                                 AtomicBoolean completed) {
        return new ResourceEventHandler<Job>() {

            @Override
            public void onAdd(Job watchedJob) {
                withTaskLogContext(() -> {
                    log.info("event received, job: {}, action: ADD", watchedJob.getMetadata().getName());
                    handleBatchJobTerminalStatus(watchedJob, taskResponse, countDownLatch, completed);
                });
            }

            @Override
            public void onUpdate(Job oldJob, Job watchedJob) {
                withTaskLogContext(() -> {
                    log.info("event received, job: {}, action: UPDATE", watchedJob.getMetadata().getName());
                    handleBatchJobTerminalStatus(watchedJob, taskResponse, countDownLatch, completed);
                });
            }

            @Override
            public void onDelete(Job watchedJob, boolean deletedFinalStateUnknown) {
                withTaskLogContext(() -> {
                    if (completed.get()) {
                        return;
                    }
                    log.info("event received, job: {}, action: DELETE", watchedJob.getMetadata().getName());
                    log.error("[K8sJobMonitor-{}] fail in k8s", watchedJob.getMetadata().getName());
                    completeOnce(completed, countDownLatch, taskResponse, EXIT_CODE_FAILURE,
                            watchedJob.getMetadata().getName());
                });
            }
        };
    }

    private void awaitJobCompletion(CountDownLatch countDownLatch) throws InterruptedException, TaskException {
        boolean timeoutFlag = taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.FAILED
                || taskRequest.getTaskTimeoutStrategy() == TaskTimeoutStrategy.WARNFAILED;
        if (timeoutFlag) {
            if (!countDownLatch.await(taskRequest.getTaskTimeout(), TimeUnit.SECONDS)) {
                throw new TaskException("K8sTask is timeout");
            }
        } else {
            countDownLatch.await();
        }
    }

    private void withTaskLogContext(Runnable action) {
        try {
            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskRequest.getWorkflowInstanceId(),
                    taskRequest.getTaskInstanceId());
            LogUtils.setTaskInstanceLogFullPathMDC(taskRequest.getLogPath());
            action.run();
        } finally {
            LogUtils.removeTaskInstanceLogFullPathMDC();
            LogUtils.removeWorkflowAndTaskInstanceIdMDC();
        }
    }

    private void handleBatchJobTerminalStatus(Job watchedJob, TaskResponse taskResponse,
                                              CountDownLatch countDownLatch,
                                              AtomicBoolean completed) {
        if (completed.get()) {
            return;
        }
        int jobStatus = getK8sJobStatus(watchedJob);
        log.info("job {} status {}", watchedJob.getMetadata().getName(), jobStatus);
        if (jobStatus == TaskConstants.RUNNING_CODE) {
            return;
        }
        completeOnce(completed, countDownLatch, taskResponse, jobStatus, watchedJob.getMetadata().getName());
    }

    private static int getK8sJobStatus(Job job) {
        JobStatus jobStatus = job.getStatus();
        if (jobStatus.getSucceeded() != null && jobStatus.getSucceeded() == 1) {
            return EXIT_CODE_SUCCESS;
        } else if (jobStatus.getFailed() != null && jobStatus.getFailed() == 1) {
            return EXIT_CODE_FAILURE;
        } else {
            return TaskConstants.RUNNING_CODE;
        }
    }

    private static void completeOnce(AtomicBoolean completed,
                                     CountDownLatch countDownLatch,
                                     TaskResponse taskResponse,
                                     int jobStatus,
                                     String jobName) {
        if (completed.compareAndSet(false, true)) {
            setTaskStatus(jobStatus, jobName, taskResponse);
            countDownLatch.countDown();
        }
    }

    private static void setTaskStatus(int jobStatus, String jobName, TaskResponse taskResponse) {
        if (jobStatus == EXIT_CODE_SUCCESS || jobStatus == EXIT_CODE_FAILURE) {
            if (jobStatus == EXIT_CODE_SUCCESS) {
                log.info("[K8sJobMonitor-{}] succeed in k8s", jobName);
                taskResponse.setExitStatusCode(EXIT_CODE_SUCCESS);
            } else {
                log.error("[K8sJobMonitor-{}] fail in k8s", jobName);
                taskResponse.setExitStatusCode(EXIT_CODE_FAILURE);
            }
        }
    }
}
