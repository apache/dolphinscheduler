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
import org.apache.dolphinscheduler.extract.master.ILogicTaskExecutorOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.exceptions.TaskKillException;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.dispatch.TaskDispatchException;
import org.apache.dolphinscheduler.task.executor.eventbus.ITaskExecutorLifecycleEventReporter;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorDispatchResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorKillResponse;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseRequest;
import org.apache.dolphinscheduler.task.executor.operations.TaskExecutorPauseResponse;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogicTaskExecutorClientDelegator implements ITaskExecutorClientDelegator {

    @Autowired
    private MasterConfig masterConfig;

    @Override
    public void dispatch(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskDispatchException {
        final String logicTaskExecutorAddress = masterConfig.getMasterAddress();
        final TaskExecutionContext taskExecutionContext = taskExecutionRunnable.getTaskExecutionContext();

        taskExecutionContext.setHost(logicTaskExecutorAddress);
        taskExecutionRunnable.getTaskInstance().setHost(logicTaskExecutorAddress);

        final TaskExecutorDispatchResponse logicTaskDispatchResponse = Clients
                .withService(ILogicTaskExecutorOperator.class)
                .withHost(logicTaskExecutorAddress)
                .dispatchTask(TaskExecutorDispatchRequest.of(taskExecutionContext));
        if (!logicTaskDispatchResponse.isDispatchSuccess()) {
            throw new TaskDispatchException(
                    String.format("Dispatch LogicTask to %s failed, response is: %s",
                            taskExecutionContext.getHost(), logicTaskDispatchResponse));
        }
    }

    @Override
    public boolean reassignMasterHost(final ITaskExecutionRunnable taskExecutionRunnable) {
        // The Logic Task doesn't support take-over, since the logic task is not executed on the worker.
        return false;
    }

    @Override
    public void pause(final ITaskExecutionRunnable taskExecutionRunnable) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final String executorHost = taskInstance.getHost();
        final String taskName = taskInstance.getName();
        checkArgument(StringUtils.isNotEmpty(executorHost), "Executor host is empty");

        final TaskExecutorPauseResponse pauseResponse = Clients
                .withService(ILogicTaskExecutorOperator.class)
                .withHost(taskInstance.getHost())
                .pauseTask(TaskExecutorPauseRequest.of(taskInstance.getId()));
        if (pauseResponse.isSuccess()) {
            log.info("Pause task {} on executor {} successfully", taskName, executorHost);
        } else {
            log.warn("Pause task {} on executor {} failed with response {}", taskName, executorHost, pauseResponse);
        }
    }

    @Override
    public void kill(final ITaskExecutionRunnable taskExecutionRunnable) throws TaskKillException {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final String executorHost = taskInstance.getHost();
        final String taskName = taskInstance.getName();
        checkArgument(StringUtils.isNotEmpty(executorHost), "Executor host is empty");

        final TaskExecutorKillResponse killResponse = Clients
                .withService(ILogicTaskExecutorOperator.class)
                .withHost(taskInstance.getHost())
                .killTask(TaskExecutorKillRequest.of(taskInstance.getId()));
        if (killResponse.isSuccess()) {
            log.info("Kill task {} on executor {} successfully", taskName, executorHost);
        } else {
            log.warn("Kill task {} on executor {} failed with response {}", taskName, executorHost, killResponse);
        }
    }

    @Override
    public void ackTaskExecutorLifecycleEvent(
                                              final ITaskExecutionRunnable taskExecutionRunnable,
                                              final ITaskExecutorLifecycleEventReporter.TaskExecutorLifecycleEventAck taskExecutorLifecycleEventAck) {
        final TaskInstance taskInstance = taskExecutionRunnable.getTaskInstance();
        final String executorHost = taskInstance.getHost();
        checkArgument(StringUtils.isNotEmpty(executorHost), "Executor host is empty");

        Clients
                .withService(ILogicTaskExecutorOperator.class)
                .withHost(taskInstance.getHost())
                .ackTaskExecutorLifecycleEvent(taskExecutorLifecycleEventAck);
    }

}
