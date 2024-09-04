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

package org.apache.dolphinscheduler.server.master.engine.task.client;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.extract.base.client.Clients;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.extract.worker.IPhysicalTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.cluster.loadbalancer.IWorkerLoadBalancer;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterResponse;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PhysicalTaskExecutorClientDelegator implements ITaskExecutorClientDelegator {

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private IWorkerLoadBalancer workerLoadBalancer;

    @Override
    public void dispatch(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskDispatchException {
        final TaskExecutionContext taskExecutionContext = taskExecutionRunnable.getTaskExecutionContext();
        final String taskName = taskExecutionContext.getTaskName();
        final String physicalTaskExecutorAddress = workerLoadBalancer
                .select(taskExecutionContext.getWorkerGroup())
                .map(Host::of)
                .map(Host::getAddress)
                .orElseThrow(() -> new TaskDispatchException(
                        String.format("Cannot find the host to dispatch Task[id=%s, name=%s]",
                                taskExecutionContext.getTaskInstanceId(), taskName)));

        taskExecutionContext.setHost(physicalTaskExecutorAddress);
        taskExecutionRunnable.getTaskInstance().setHost(physicalTaskExecutorAddress);

        try {
            final TaskExecutorDispatchResponse taskExecutorDispatchResponse = Clients
                    .withService(IPhysicalTaskExecutorOperator.class)
                    .withHost(physicalTaskExecutorAddress)
                    .dispatchTask(TaskExecutorDispatchRequest.of(taskExecutionRunnable.getTaskExecutionContext()));
            if (!taskExecutorDispatchResponse.isDispatchSuccess()) {
                throw new TaskDispatchException(
                        "Dispatch task: " + taskName + " to " + physicalTaskExecutorAddress + " failed: "
                                + taskExecutorDispatchResponse);
            }
        } catch (TaskDispatchException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskDispatchException(
                    "Dispatch task: " + taskName + " to " + physicalTaskExecutorAddress + " failed", e);
        }
    }

    @Override
    public boolean reassignMasterHost(final ITaskExecutionRunnable taskExecutionRunnable) {
        final String taskName = taskExecutionRunnable.getName();
        checkArgument(taskExecutionRunnable.isTaskInstanceInitialized(),
                "Task " + taskName + "is not initialized cannot take-over");

        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final String taskExecutorHost = taskInstance.getHost();
        if (StringUtils.isEmpty(taskExecutorHost)) {
            log.debug(
                    "The task executor: {} host is empty, cannot take-over, this might caused by the task hasn't dispatched",
                    taskName);
            return false;
        }

        final TaskExecutorReassignMasterRequest taskExecutorReassignMasterRequest =
                TaskExecutorReassignMasterRequest.builder()
                        .taskInstanceId(taskInstance.getId())
                        .workflowHost(masterConfig.getMasterAddress())
                        .build();
        final TaskExecutorReassignMasterResponse taskExecutorReassignMasterResponse =
                Clients
                        .withService(IPhysicalTaskExecutorOperator.class)
                        .withHost(taskInstance.getHost())
                        .reassignWorkflowInstanceHost(taskExecutorReassignMasterRequest);
        boolean success = taskExecutorReassignMasterResponse.isSuccess();
        if (success) {
            log.info("Reassign master host {} to {} successfully", taskExecutorHost, taskName);
        } else {
            log.info("Reassign master host {} on {} failed with response {}",
                    taskExecutorHost,
                    taskName,
                    taskExecutorReassignMasterResponse);
        }
        return success;
    }

    @Override
    public void pause(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final String executorHost = taskInstance.getHost();
        final String taskName = taskInstance.getName();
        checkArgument(StringUtils.isNotEmpty(executorHost), "Executor host is empty");

        final TaskExecutorPauseResponse pauseResponse = Clients
                .withService(IPhysicalTaskExecutorOperator.class)
                .withHost(taskInstance.getHost())
                .pauseTask(TaskExecutorPauseRequest.of(taskInstance.getId()));
        if (pauseResponse.isSuccess()) {
            log.info("Pause task {} on executor {} successfully", taskName, executorHost);
        } else {
            log.warn("Pause task {} on executor {} failed with response {}", taskName, executorHost, pauseResponse);
        }
    }

    @Override
    public void kill(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final String executorHost = taskInstance.getHost();
        final String taskName = taskInstance.getName();
        checkArgument(StringUtils.isNotEmpty(executorHost), "Executor host is empty");

        final TaskExecutorKillResponse killResponse = Clients
                .withService(IPhysicalTaskExecutorOperator.class)
                .withHost(executorHost)
                .killTask(TaskExecutorKillRequest.of(taskInstance.getId()));
        if (killResponse.isSuccess()) {
            log.info("Kill task {} on executor {} successfully", taskName, executorHost);
        } else {
            log.warn("Kill task {} on executor {} failed with response {}", taskName, executorHost, killResponse);
        }
    }

    @Override
    public void ackTaskExecutorLifecycleEvent(final ITaskExecutionRunnable taskExecutionRunnable,
                                              final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final String executorHost = taskInstance.getHost();
        checkArgument(StringUtils.isNotEmpty(executorHost), "Executor host is empty");

        Clients
                .withService(IPhysicalTaskExecutorOperator.class)
                .withHost(executorHost)
                .ackPhysicalTaskExecutorLifecycleEvent(taskExecutorLifecycleEventAck);
    }

}
