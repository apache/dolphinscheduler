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

package org.apache.dolphinscheduler.server.master.runner;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.extract.base.utils.Host;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.dispatch.exceptions.WorkerGroupNotFoundException;
import org.apache.dolphinscheduler.server.master.exception.TaskDispatchException;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEventService;
import org.apache.dolphinscheduler.server.master.runner.dispatcher.TaskDispatcher;
import org.apache.dolphinscheduler.server.master.runner.execute.TaskExecuteRunnable;

import java.util.Date;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseTaskDispatcher implements TaskDispatcher {

    protected final TaskEventService taskEventService;
    protected final MasterConfig masterConfig;

    protected BaseTaskDispatcher(TaskEventService taskEventService,
                                 MasterConfig masterConfig) {
        this.taskEventService = checkNotNull(taskEventService);
        this.masterConfig = checkNotNull(masterConfig);
    }

    @Override
    public void dispatchTask(TaskExecuteRunnable taskExecuteRunnable) throws TaskDispatchException {
        Host taskInstanceDispatchHost;
        try {
            taskInstanceDispatchHost = getTaskInstanceDispatchHost(taskExecuteRunnable)
                    .orElseThrow(() -> new TaskDispatchException("Cannot find the host to execute task."));
        } catch (WorkerGroupNotFoundException workerGroupNotFoundException) {
            log.error("Dispatch task: {} failed, worker group not found.",
                    taskExecuteRunnable.getTaskExecutionContext().getTaskName(), workerGroupNotFoundException);
            addDispatchFailedEvent(taskExecuteRunnable);
            return;
        }
        taskExecuteRunnable.getTaskExecutionContext().setHost(taskInstanceDispatchHost.getAddress());
        doDispatch(taskExecuteRunnable);
        taskExecuteRunnable.getTaskInstance().setHost(taskInstanceDispatchHost.getAddress());
        log.info("Success dispatch task {} to {}.", taskExecuteRunnable.getTaskExecutionContext().getTaskName(),
                taskInstanceDispatchHost.getAddress());
        addDispatchEvent(taskExecuteRunnable);
    }

    protected abstract void doDispatch(TaskExecuteRunnable taskExecuteRunnable) throws TaskDispatchException;

    protected abstract Optional<Host> getTaskInstanceDispatchHost(TaskExecuteRunnable taskExecutionContext) throws TaskDispatchException, WorkerGroupNotFoundException;

    protected void addDispatchEvent(TaskExecuteRunnable taskExecuteRunnable) {
        TaskExecutionContext taskExecutionContext = taskExecuteRunnable.getTaskExecutionContext();
        TaskEvent taskEvent = TaskEvent.newDispatchEvent(
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId(),
                taskExecutionContext.getHost());
        taskEventService.addEvent(taskEvent);
    }

    private void addDispatchFailedEvent(TaskExecuteRunnable taskExecuteRunnable) {
        TaskExecutionContext taskExecutionContext = taskExecuteRunnable.getTaskExecutionContext();
        TaskEvent taskEvent = TaskEvent.builder()
                .processInstanceId(taskExecutionContext.getProcessInstanceId())
                .taskInstanceId(taskExecutionContext.getTaskInstanceId())
                .state(TaskExecutionStatus.FAILURE)
                .logPath(taskExecutionContext.getLogPath())
                .executePath(taskExecutionContext.getExecutePath())
                .appIds(taskExecutionContext.getAppIds())
                .processId(taskExecutionContext.getProcessId())
                .varPool(taskExecutionContext.getVarPool())
                .startTime(DateUtils.timeStampToDate(taskExecutionContext.getStartTime()))
                .endTime(new Date())
                .event(TaskEventType.RESULT)
                .build();
        taskEventService.addEvent(taskEvent);
    }
}
