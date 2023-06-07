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

package org.apache.dolphinscheduler.server.master.event;

import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.DefaultTaskExecuteRunnable;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(StateEventHandler.class)
@Slf4j
public class TaskTimeoutStateEventHandler implements StateEventHandler {

    @Override
    public boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable,
                                    StateEvent stateEvent) throws StateEventHandleError, StateEventHandleException {
        TaskStateEvent taskStateEvent = (TaskStateEvent) stateEvent;

        TaskMetrics.incTaskInstanceByState("timeout");
        workflowExecuteRunnable.checkTaskInstanceByStateEvent(taskStateEvent);

        TaskInstance taskInstance =
                workflowExecuteRunnable.getTaskInstance(taskStateEvent.getTaskInstanceId()).orElseThrow(
                        () -> new StateEventHandleError(String.format(
                                "Cannot find the task instance from workflow execute runnable, taskInstanceId: %s",
                                taskStateEvent.getTaskInstanceId())));

        log.info("Handle task instance state timeout event, taskInstanceId: {}", taskStateEvent.getTaskInstanceId());

        if (TimeoutFlag.CLOSE == taskInstance.getTaskDefine().getTimeoutFlag()) {
            return true;
        }
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine()
                .getTimeoutNotifyStrategy();
        Map<Long, DefaultTaskExecuteRunnable> taskExecuteRunnableMap =
                workflowExecuteRunnable.getTaskExecuteRunnableMap();
        if ((TaskTimeoutStrategy.FAILED == taskTimeoutStrategy
                || TaskTimeoutStrategy.WARNFAILED == taskTimeoutStrategy)) {
            if (taskExecuteRunnableMap.containsKey(taskInstance.getTaskCode())) {
                taskExecuteRunnableMap.get(taskInstance.getTaskCode()).timeout();
                workflowExecuteRunnable.taskFinished(taskInstance);
            } else {
                log.warn(
                        "cannot find the task processor for task {}, so skip task processor action.",
                        taskInstance.getTaskCode());
            }
        }
        if (TaskTimeoutStrategy.WARN == taskTimeoutStrategy
                || TaskTimeoutStrategy.WARNFAILED == taskTimeoutStrategy) {
            workflowExecuteRunnable.processTimeout();
            workflowExecuteRunnable.taskTimeout(taskInstance);
        }
        return true;
    }

    @Override
    public StateEventType getEventType() {
        return StateEventType.TASK_TIMEOUT;
    }
}
