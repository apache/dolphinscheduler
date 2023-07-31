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

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.utils.TaskUtils;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.utils.ProcessUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkerFailoverService {

    private final RegistryClient registryClient;
    private final MasterConfig masterConfig;
    private final ProcessService processService;
    private final WorkflowExecuteThreadPool workflowExecuteThreadPool;
    private final ProcessInstanceExecCacheManager cacheManager;
    private final LogClient logClient;
    private final String localAddress;

    private final TaskInstanceDao taskInstanceDao;

    public WorkerFailoverService(@NonNull RegistryClient registryClient,
                                 @NonNull MasterConfig masterConfig,
                                 @NonNull ProcessService processService,
                                 @NonNull WorkflowExecuteThreadPool workflowExecuteThreadPool,
                                 @NonNull ProcessInstanceExecCacheManager cacheManager,
                                 @NonNull LogClient logClient,
                                 @NonNull TaskInstanceDao taskInstanceDao) {
        this.registryClient = registryClient;
        this.masterConfig = masterConfig;
        this.processService = processService;
        this.workflowExecuteThreadPool = workflowExecuteThreadPool;
        this.cacheManager = cacheManager;
        this.logClient = logClient;
        this.localAddress = masterConfig.getMasterAddress();
        this.taskInstanceDao = taskInstanceDao;
    }

    /**
     * Do the worker failover. Will find the SUBMITTED_SUCCESS/DISPATCH/RUNNING_EXECUTION/DELAY_EXECUTION/READY_PAUSE/READY_STOP tasks belong the given worker,
     * and failover these tasks.
     * <p>
     * Note: When we do worker failover, the master will only failover the processInstance belongs to the current master.
     *
     * @param workerHost worker host
     */
    public void failoverWorker(@NonNull String workerHost) {
        log.info("Worker[{}] failover starting", workerHost);
        final StopWatch failoverTimeCost = StopWatch.createStarted();

        // we query the task instance from cache, so that we can directly update the cache
        final Optional<Date> needFailoverWorkerStartTime =
                getServerStartupTime(registryClient.getServerList(RegistryNodeType.WORKER), workerHost);

        final List<TaskInstance> needFailoverTaskInstanceList = getNeedFailoverTaskInstance(workerHost);
        if (CollectionUtils.isEmpty(needFailoverTaskInstanceList)) {
            log.info("Worker[{}] failover finished there are no taskInstance need to failover", workerHost);
            return;
        }
        log.info(
                "Worker[{}] failover there are {} taskInstance may need to failover, will do a deep check, taskInstanceIds: {}",
                workerHost,
                needFailoverTaskInstanceList.size(),
                needFailoverTaskInstanceList.stream().map(TaskInstance::getId).collect(Collectors.toList()));
        final Map<Integer, ProcessInstance> processInstanceCacheMap = new HashMap<>();
        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            try (
                    final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskInstance.getProcessInstanceId(),
                                    taskInstance.getId())) {
                try {
                    ProcessInstance processInstance = processInstanceCacheMap.computeIfAbsent(
                            taskInstance.getProcessInstanceId(), k -> {
                                WorkflowExecuteRunnable workflowExecuteRunnable = cacheManager.getByProcessInstanceId(
                                        taskInstance.getProcessInstanceId());
                                if (workflowExecuteRunnable == null) {
                                    return null;
                                }
                                return workflowExecuteRunnable.getWorkflowExecuteContext()
                                        .getWorkflowInstance();
                            });
                    if (!checkTaskInstanceNeedFailover(needFailoverWorkerStartTime, processInstance, taskInstance)) {
                        log.info("Worker[{}] the current taskInstance doesn't need to failover", workerHost);
                        continue;
                    }
                    log.info(
                            "Worker[{}] failover: begin to failover taskInstance, will set the status to NEED_FAULT_TOLERANCE",
                            workerHost);
                    failoverTaskInstance(processInstance, taskInstance);
                    log.info("Worker[{}] failover: Finish failover taskInstance", workerHost);
                } catch (Exception ex) {
                    log.info("Worker[{}] failover taskInstance occur exception", workerHost, ex);
                }
            }
        }
        failoverTimeCost.stop();
        log.info("Worker[{}] failover finished, useTime:{}ms",
                workerHost,
                failoverTimeCost.getTime(TimeUnit.MILLISECONDS));
    }

    /**
     * failover task instance
     * <p>
     * 1. kill yarn/k8s job if run on worker and there are yarn/k8s jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. try to notify local master
     *
     * @param processInstance
     * @param taskInstance
     */
    private void failoverTaskInstance(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskMetrics.incTaskInstanceByState("failover");

        taskInstance.setProcessInstance(processInstance);

        if (!TaskUtils.isMasterTask(taskInstance.getTaskType())) {
            log.info("The failover taskInstance is not master task");
            TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                    .buildWorkflowInstanceHost(masterConfig.getMasterAddress())
                    .buildTaskInstanceRelatedInfo(taskInstance)
                    .buildProcessInstanceRelatedInfo(processInstance)
                    .buildProcessDefinitionRelatedInfo(processInstance.getProcessDefinition())
                    .create();

            if (masterConfig.isKillApplicationWhenTaskFailover()) {
                // only kill yarn/k8s job if exists , the local thread has exited
                log.info("TaskInstance failover begin kill the task related yarn or k8s job");
                ProcessUtils.killApplication(logClient, taskExecutionContext);
            }
        } else {
            log.info("The failover taskInstance is a master task, no need to failover in worker failover");
        }

        taskInstance.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);
        taskInstance.setFlag(Flag.NO);
        taskInstanceDao.upsertTaskInstance(taskInstance);

        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(processInstance.getId())
                .taskInstanceId(taskInstance.getId())
                .status(TaskExecutionStatus.NEED_FAULT_TOLERANCE)
                .type(StateEventType.TASK_STATE_CHANGE)
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    /**
     * task needs failover if task start before server starts
     *
     * @return true if task instance need fail over
     */
    private boolean checkTaskInstanceNeedFailover(Optional<Date> needFailoverWorkerStartTime,
                                                  @Nullable ProcessInstance processInstance,
                                                  TaskInstance taskInstance) {
        if (processInstance == null) {
            log.error(
                    "Failover task instance error, cannot find the related processInstance form memory, this case shouldn't happened");
            return false;
        }
        if (taskInstance == null) {
            log.error("Master failover task instance error, taskInstance is null, this case shouldn't happened");
            return false;
        }
        // only failover the task owned myself if worker down.
        if (!StringUtils.equalsIgnoreCase(processInstance.getHost(), localAddress)) {
            log.error(
                    "Master failover task instance error, the taskInstance's processInstance's host: {} is not the current master: {}",
                    processInstance.getHost(),
                    localAddress);
            return false;
        }
        if (taskInstance.getState() != null && taskInstance.getState().isFinished()) {
            // The taskInstance is already finished, doesn't need to failover
            log.info("The task is already finished, doesn't need to failover");
            return false;
        }
        if (!needFailoverWorkerStartTime.isPresent()) {
            // The worker is still down
            return true;
        }
        // The worker is active, may already send some new task to it
        if (taskInstance.getSubmitTime() != null && taskInstance.getSubmitTime()
                .after(needFailoverWorkerStartTime.get())) {
            log.info(
                    "The taskInstance's submitTime: {} is after the need failover worker's start time: {}, the taskInstance is newly submit, it doesn't need to failover",
                    taskInstance.getSubmitTime(),
                    needFailoverWorkerStartTime.get());
            return false;
        }

        return true;
    }

    private List<TaskInstance> getNeedFailoverTaskInstance(@NonNull String failoverWorkerHost) {
        // we query the task instance from cache, so that we can directly update the cache
        return cacheManager.getAll()
                .stream()
                .flatMap(workflowExecuteRunnable -> workflowExecuteRunnable.getAllTaskInstances().stream())
                // If the worker is in dispatching and the host is not set
                .filter(taskInstance -> failoverWorkerHost.equals(taskInstance.getHost())
                        && taskInstance.getState().shouldFailover())
                .collect(Collectors.toList());
    }

    private Optional<Date> getServerStartupTime(List<Server> servers, String host) {
        if (CollectionUtils.isEmpty(servers)) {
            return Optional.empty();
        }
        Date serverStartupTime = null;
        for (Server server : servers) {
            if (host.equals(server.getHost() + Constants.COLON + server.getPort())) {
                serverStartupTime = server.getCreateTime();
                break;
            }
        }
        return Optional.ofNullable(serverStartupTime);
    }
}
