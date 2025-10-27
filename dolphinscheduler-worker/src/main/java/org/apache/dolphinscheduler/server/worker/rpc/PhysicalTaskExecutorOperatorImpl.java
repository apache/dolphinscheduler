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

import org.apache.dolphinscheduler.extract.worker.IPhysicalTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.executor.PhysicalTaskEngineDelegator;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorReassignMasterResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

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
        final TaskExecutionContext taskExecutionContext = taskExecutorDispatchRequest.getTaskExecutionContext();
        final int taskInstanceId = taskExecutionContext.getTaskInstanceId();
        final int workflowInstanceId = taskExecutionContext.getWorkflowInstanceId();

        try (
                TaskExecutorMDCUtils.MDCAutoClosable _ignore1 =
                        TaskExecutorMDCUtils.logWithMDC(taskInstanceId, workflowInstanceId)) {

            log.info("Receive TaskExecutorDispatchResponse: {}", taskExecutorDispatchRequest);
            physicalTaskEngineDelegator.dispatchLogicTask(taskExecutionContext);

            // Reset MDC, because dispatchLogicTask will clear MDC internally
            TaskExecutorMDCUtils.logWithMDC(taskInstanceId, workflowInstanceId);

            log.info("Handle TaskExecutorDispatchResponse: {} success", taskExecutorDispatchRequest);
            return TaskExecutorDispatchResponse.success();
        } catch (Throwable throwable) {
            log.error("Handle TaskExecutorDispatchResponse: {} failed", taskExecutorDispatchRequest, throwable);
            return TaskExecutorDispatchResponse.failed(ExceptionUtils.getMessage(throwable));
        }
    }

    @Override
    public TaskExecutorKillResponse killTask(final TaskExecutorKillRequest taskExecutorKillRequest) {
        final int taskInstanceId = taskExecutorKillRequest.getTaskInstanceId();
        final int workflowInstanceId = physicalTaskEngineDelegator.getWorkflowInstanceId(taskInstanceId);
        try (
                TaskExecutorMDCUtils.MDCAutoClosable ignore =
                        TaskExecutorMDCUtils.logWithMDC(taskInstanceId, workflowInstanceId)) {
            log.info("Receive TaskExecutorKillRequest: {}", taskExecutorKillRequest);
            physicalTaskEngineDelegator.killLogicTask(taskInstanceId);
            log.info("Handle TaskExecutorKillRequest: {} success", taskExecutorKillRequest);
            return TaskExecutorKillResponse.success();
        } catch (Throwable throwable) {
            log.error("Handle TaskExecutorKillRequest: {} failed", taskExecutorKillRequest, throwable);
            return TaskExecutorKillResponse.fail(ExceptionUtils.getMessage(throwable));
        }
    }

    @Override
    public TaskExecutorPauseResponse pauseTask(final TaskExecutorPauseRequest taskPauseRequest) {
        final int taskInstanceId = taskPauseRequest.getTaskInstanceId();
        final int workflowInstanceId = physicalTaskEngineDelegator.getWorkflowInstanceId(taskInstanceId);
        try (
                TaskExecutorMDCUtils.MDCAutoClosable ignore =
                        TaskExecutorMDCUtils.logWithMDC(taskInstanceId, workflowInstanceId)) {
            log.info("Receive TaskExecutorPauseRequest: {}", taskPauseRequest);
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
        final int taskInstanceId = taskExecutorReassignMasterRequest.getTaskInstanceId();
        try (TaskExecutorMDCUtils.MDCAutoClosable ignore = TaskExecutorMDCUtils.logWithMDC(taskInstanceId)) {
            boolean success =
                    physicalTaskEngineDelegator.reassignWorkflowInstanceHost(taskExecutorReassignMasterRequest);
            if (success) {
                return TaskExecutorReassignMasterResponse.success();
            }
            return TaskExecutorReassignMasterResponse.failed("Reassign master host failed");
        }
    }

    @Override
    public void ackPhysicalTaskExecutorLifecycleEvent(final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck) {
        final int taskExecutorId = taskExecutorLifecycleEventAck.getTaskExecutorId();
        final int workflowInstanceId = physicalTaskEngineDelegator.getWorkflowInstanceId(taskExecutorId);
        try (
                TaskExecutorMDCUtils.MDCAutoClosable ignore =
                        TaskExecutorMDCUtils.logWithMDC(taskExecutorId, workflowInstanceId)) {
            log.info("Receive TaskExecutorLifecycleEventAck: {}", taskExecutorLifecycleEventAck);
            physicalTaskEngineDelegator.ackPhysicalTaskExecutorLifecycleEventACK(taskExecutorLifecycleEventAck);
        }
    }
}
