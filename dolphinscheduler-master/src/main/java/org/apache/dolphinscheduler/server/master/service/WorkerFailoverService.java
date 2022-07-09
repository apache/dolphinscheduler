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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.LoggerUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.runner.task.TaskProcessorFactory;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import lombok.NonNull;

@Service
public class WorkerFailoverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerFailoverService.class);

    private final RegistryClient registryClient;
    private final MasterConfig masterConfig;
    private final ProcessService processService;
    private final WorkflowExecuteThreadPool workflowExecuteThreadPool;
    private final ProcessInstanceExecCacheManager cacheManager;
    private final String localAddress;

    public WorkerFailoverService(@NonNull RegistryClient registryClient,
                                 @NonNull MasterConfig masterConfig,
                                 @NonNull ProcessService processService,
                                 @NonNull WorkflowExecuteThreadPool workflowExecuteThreadPool,
                                 @NonNull ProcessInstanceExecCacheManager cacheManager) {
        this.registryClient = registryClient;
        this.masterConfig = masterConfig;
        this.processService = processService;
        this.workflowExecuteThreadPool = workflowExecuteThreadPool;
        this.cacheManager = cacheManager;
        this.localAddress = NetUtils.getAddr(masterConfig.getListenPort());
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
        LOGGER.info("Worker[{}] failover starting", workerHost);
        final StopWatch failoverTimeCost = StopWatch.createStarted();

        // we query the task instance from cache, so that we can directly update the cache
        final Optional<Date> needFailoverWorkerStartTime =
            getServerStartupTime(registryClient.getServerList(NodeType.WORKER), workerHost);

        final List<TaskInstance> needFailoverTaskInstanceList = getNeedFailoverTaskInstance(workerHost);
        if (CollectionUtils.isEmpty(needFailoverTaskInstanceList)) {
            LOGGER.info("Worker[{}] failover finished there are no taskInstance need to failover", workerHost);
            return;
        }
        LOGGER.info(
            "Worker[{}] failover there are {} taskInstance may need to failover, will do a deep check, taskInstanceIds: {}",
            workerHost,
            needFailoverTaskInstanceList.size(),
            needFailoverTaskInstanceList.stream().map(TaskInstance::getId).collect(Collectors.toList()));
        final Map<Integer, ProcessInstance> processInstanceCacheMap = new HashMap<>();
        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            LoggerUtils.setWorkflowAndTaskInstanceIDMDC(taskInstance.getProcessInstanceId(), taskInstance.getId());
            try {
                ProcessInstance processInstance =
                    processInstanceCacheMap.computeIfAbsent(taskInstance.getProcessInstanceId(), k -> {
                        WorkflowExecuteRunnable workflowExecuteRunnable =
                            cacheManager.getByProcessInstanceId(taskInstance.getProcessInstanceId());
                        if (workflowExecuteRunnable == null) {
                            return null;
                        }
                        return workflowExecuteRunnable.getProcessInstance();
                    });
                if (!checkTaskInstanceNeedFailover(needFailoverWorkerStartTime, processInstance, taskInstance)) {
                    LOGGER.info("Worker[{}] the current taskInstance doesn't need to failover", workerHost);
                    continue;
                }
                LOGGER.info(
                    "Worker[{}] failover: begin to failover taskInstance, will set the status to NEED_FAULT_TOLERANCE",
                    workerHost);
                failoverTaskInstance(processInstance, taskInstance);
                LOGGER.info("Worker[{}] failover: Finish failover taskInstance", workerHost);
            } catch (Exception ex) {
                LOGGER.info("Worker[{}] failover taskInstance occur exception", workerHost, ex);
            } finally {
                LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
        failoverTimeCost.stop();
        LOGGER.info("Worker[{}] failover finished, useTime:{}ms",
            workerHost,
            failoverTimeCost.getTime(TimeUnit.MILLISECONDS));
    }

    /**
     * failover task instance
     * <p>
     * 1. kill yarn job if run on worker and there are yarn jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. try to notify local master
     *
     * @param processInstance
     * @param taskInstance
     */
    private void failoverTaskInstance(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {

        TaskMetrics.incTaskFailover();
        boolean isMasterTask = TaskProcessorFactory.isMasterTask(taskInstance.getTaskType());

        taskInstance.setProcessInstance(processInstance);

        if (!isMasterTask) {
            LOGGER.info("The failover taskInstance is not master task");
            TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(processInstance)
                .create();

            if (masterConfig.isKillYarnJobWhenTaskFailover()) {
                // only kill yarn job if exists , the local thread has exited
                LOGGER.info("TaskInstance failover begin kill the task related yarn job");
                ProcessUtils.killYarnJob(taskExecutionContext);
            }
        } else {
            LOGGER.info("The failover taskInstance is a master task");
        }

        taskInstance.setState(ExecutionStatus.NEED_FAULT_TOLERANCE);
        taskInstance.setFlag(Flag.NO);
        processService.saveTaskInstance(taskInstance);

        StateEvent stateEvent = new StateEvent();
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(processInstance.getId());
        stateEvent.setExecutionStatus(taskInstance.getState());
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
            // This case should be happened.
            LOGGER.error(
                "Failover task instance error, cannot find the related processInstance form memory, this case shouldn't happened");
            return false;
        }
        if (taskInstance == null) {
            // This case should be happened.
            LOGGER.error("Master failover task instance error, taskInstance is null, this case shouldn't happened");
            return false;
        }
        // only failover the task owned myself if worker down.
        if (!StringUtils.equalsIgnoreCase(processInstance.getHost(), localAddress)) {
            LOGGER.error(
                "Master failover task instance error, the taskInstance's processInstance's host: {} is not the current master: {}",
                processInstance.getHost(),
                localAddress);
            return false;
        }
        if (taskInstance.getState() != null && taskInstance.getState().typeIsFinished()) {
            // The taskInstance is already finished, doesn't need to failover
            LOGGER.info("The task is already finished, doesn't need to failover");
            return false;
        }
        if (!needFailoverWorkerStartTime.isPresent()) {
            // The worker is still down
            return true;
        }
        // The worker is active, may already send some new task to it
        if (taskInstance.getSubmitTime() != null && taskInstance.getSubmitTime()
            .after(needFailoverWorkerStartTime.get())) {
            LOGGER.info(
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
                && ExecutionStatus.isNeedFailoverWorkflowInstanceState(taskInstance.getState()))
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
