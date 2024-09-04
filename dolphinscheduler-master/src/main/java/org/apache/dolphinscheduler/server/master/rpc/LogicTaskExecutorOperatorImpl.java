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

package org.apache.dolphinscheduler.server.master.rpc;

import org.apache.dolphinscheduler.extract.master.ILogicTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.engine.executor.LogicTaskEngineDelegator;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.log.TaskExecutorMDCUtils;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LogicTaskExecutorOperatorImpl implements ILogicTaskExecutorOperator {

    @Autowired
    private LogicTaskEngineDelegator logicTaskEngineDelegator;

    @Override
    public TaskExecutorDispatchResponse dispatchTask(final TaskExecutorDispatchRequest taskExecutorDispatchRequest) {
        final TaskExecutionContext taskExecutionContext = taskExecutorDispatchRequest.getTaskExecutionContext();
        try (
                final TaskExecutorMDCUtils.MDCAutoClosable ignore =
                        TaskExecutorMDCUtils.logWithMDC(taskExecutionContext.getTaskInstanceId())) {
            log.info("Receive TaskExecutorDispatchRequest: {}", taskExecutorDispatchRequest);
            try {
                logicTaskEngineDelegator.dispatchLogicTask(taskExecutionContext);
                log.info("Handle {} success", taskExecutorDispatchRequest);
                return TaskExecutorDispatchResponse.success();
            } catch (Throwable throwable) {
                log.error("Handle {} failed", taskExecutorDispatchRequest, throwable);
                return TaskExecutorDispatchResponse.failed(ExceptionUtils.getMessage(throwable));
            }
        }
    }

    @Override
    public TaskExecutorPauseResponse pauseTask(final TaskExecutorPauseRequest taskPauseRequest) {
        final int taskInstanceId = taskPauseRequest.getTaskInstanceId();
        try (final TaskExecutorMDCUtils.MDCAutoClosable ignore = TaskExecutorMDCUtils.logWithMDC(taskInstanceId)) {
            log.info("Receive {}", taskPauseRequest);
            try {
                logicTaskEngineDelegator.pauseLogicTask(taskInstanceId);
                log.info("Handle {} success", taskPauseRequest);
                return TaskExecutorPauseResponse.success();
            } catch (Throwable throwable) {
                log.error("Handle {} failed", taskPauseRequest, throwable);
                return TaskExecutorPauseResponse.fail(ExceptionUtils.getMessage(throwable));
            }
        }
    }

    @Override
    public void ackTaskExecutorLifecycleEvent(final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck) {
        final int taskExecutorId = taskExecutorLifecycleEventAck.getTaskExecutorId();
        try (final TaskExecutorMDCUtils.MDCAutoClosable ignore = TaskExecutorMDCUtils.logWithMDC(taskExecutorId)) {
            log.info("Receive TaskExecutorLifecycleEventAck: {}", taskExecutorLifecycleEventAck);
            logicTaskEngineDelegator.ackLogicTaskExecutionEvent(taskExecutorLifecycleEventAck);
        }
    }

    @Override
    public TaskExecutorKillResponse killTask(final TaskExecutorKillRequest taskKillRequest) {
        final int taskInstanceId = taskKillRequest.getTaskInstanceId();
        try (final TaskExecutorMDCUtils.MDCAutoClosable ignore = TaskExecutorMDCUtils.logWithMDC(taskInstanceId)) {
            log.info("Receive TaskExecutorKillRequest: {}", taskKillRequest);
            try {
                logicTaskEngineDelegator.killLogicTask(taskInstanceId);
                log.info("Handle TaskExecutorKillRequest: {} success", taskKillRequest);
                return TaskExecutorKillResponse.success();
            } catch (Throwable throwable) {
                log.error("Handle TaskExecutorKillRequest: {} failed", taskKillRequest, throwable);
                return TaskExecutorKillResponse.fail(ExceptionUtils.getMessage(throwable));
            }
        }
    }

}
