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

package org.apache.dolphinscheduler.server.worker.rpc;

import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.extract.worker.IPhysicalTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.executor.PhysicalTaskEngineDelegator;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.exceptions.TaskExecutorNotFoundException;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PhysicalTaskExecutorOperatorImpl implements IPhysicalTaskExecutorOperator {

    @Autowired
    private PhysicalTaskEngineDelegator physicalTaskEngineDelegator;

    @Override
    public TaskExecutorDispatchResponse dispatchTask(final TaskExecutorDispatchRequest taskExecutorDispatchRequest) {
        log.info("Receive TaskExecutorDispatchResponse: {}", taskExecutorDispatchRequest);
        final TaskExecutionContext taskExecutionContext = taskExecutorDispatchRequest.getTaskExecutionContext();
        try {
            physicalTaskEngineDelegator.dispatchLogicTask(taskExecutionContext);
            log.info("Handle TaskExecutorDispatchResponse: {} success", taskExecutorDispatchRequest);
            return TaskExecutorDispatchResponse.success();
        } catch (Throwable throwable) {
            log.error("Handle TaskExecutorDispatchResponse: {} failed", taskExecutorDispatchRequest, throwable);
            return TaskExecutorDispatchResponse.failed(ExceptionUtils.getMessage(throwable));
        }
    }

    /**
     * The time the kill waits for the cancellation before it reports that the task is not confirmed to
     * be stopped. A cancellation which takes longer, for example a task which submitted many jobs,
     * keeps running, but it cannot hold the rpc thread of the worker any longer.
     */
    private static final long KILL_WAIT_TIMEOUT_MILLIS = 90_000L;

    /**
     * The cancellation of a task runs on its own threads: a stop waits for the command which cancels
     * the job of the task, and the rpc threads of the worker must stay free for the other requests.
     */
    private static final ExecutorService KILL_EXECUTOR =
            Executors.newCachedThreadPool(ThreadUtils.newDaemonThreadFactory("physical-task-kill-%d"));

    @Override
    public TaskExecutorKillResponse killTask(final TaskExecutorKillRequest taskExecutorKillRequest) {
        log.info("Receive TaskExecutorKillRequest: {}", taskExecutorKillRequest);
        final int taskInstanceId = taskExecutorKillRequest.getTaskInstanceId();
        final Future<?> killFuture =
                KILL_EXECUTOR.submit(() -> physicalTaskEngineDelegator.killLogicTask(taskInstanceId));
        try {
            killFuture.get(KILL_WAIT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            log.info("Handle TaskExecutorKillRequest: {} success", taskExecutorKillRequest);
            return TaskExecutorKillResponse.success();
        } catch (TimeoutException e) {
            log.error("Handle TaskExecutorKillRequest: {} did not finish within {}ms, the cancellation keeps running",
                    taskExecutorKillRequest, KILL_WAIT_TIMEOUT_MILLIS);
            return TaskExecutorKillResponse.fail(String.format(
                    "The cancellation of the task did not finish within %ds, the task may still be running",
                    TimeUnit.MILLISECONDS.toSeconds(KILL_WAIT_TIMEOUT_MILLIS)));
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof TaskExecutorNotFoundException) {
                // the task executor is removed once the task is finalized: a task which is not running
                // any more does not have to be stopped
                log.warn("Task instance {} is not running any more, it does not have to be stopped", taskInstanceId);
                return TaskExecutorKillResponse.success();
            }
            log.error("Handle TaskExecutorKillRequest: {} failed", taskExecutorKillRequest, cause);
            return TaskExecutorKillResponse.fail(ExceptionUtils.getMessage(cause));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Handle TaskExecutorKillRequest: {} interrupted", taskExecutorKillRequest, e);
            return TaskExecutorKillResponse.fail("The cancellation of the task was interrupted");
        }
    }

    @Override
    public TaskExecutorPauseResponse pauseTask(final TaskExecutorPauseRequest taskPauseRequest) {
        log.info("Receive TaskExecutorPauseRequest: {}", taskPauseRequest);
        final int taskInstanceId = taskPauseRequest.getTaskInstanceId();
        try {
            physicalTaskEngineDelegator.pauseLogicTask(taskInstanceId);
            log.info("Handle TaskExecutorPauseRequest: {} success", taskPauseRequest);
            return TaskExecutorPauseResponse.success();
        } catch (Throwable throwable) {
            log.error("Handle TaskExecutorPauseRequest: {} failed", taskPauseRequest, throwable);
            return TaskExecutorPauseResponse.fail(ExceptionUtils.getMessage(throwable));
        }
    }

    @Override
    public TaskExecutorReassignMasterResponse reassignWorkflowInstanceHost(final TaskExecutorReassignMasterRequest taskExecutorReassignMasterRequest) {
        boolean success =
                physicalTaskEngineDelegator.reassignWorkflowInstanceHost(taskExecutorReassignMasterRequest);
        if (success) {
            return TaskExecutorReassignMasterResponse.success();
        }
        return TaskExecutorReassignMasterResponse.failed("Reassign master host failed");
    }

    @Override
    public void ackPhysicalTaskExecutorLifecycleEvent(final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck) {
        log.info("Receive TaskExecutorLifecycleEventAck: {}", taskExecutorLifecycleEventAck);
        physicalTaskEngineDelegator.ackPhysicalTaskExecutorLifecycleEventACK(taskExecutorLifecycleEventAck);
    }
}
