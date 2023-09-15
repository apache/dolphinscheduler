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
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import com.google.auto.service.AutoService;

@AutoService(StateEventHandler.class)
@Slf4j
public class TaskStateEventHandler implements StateEventHandler {

    @Override
    public boolean handleStateEvent(WorkflowExecuteRunnable workflowExecuteRunnable,
                                    StateEvent stateEvent) throws StateEventHandleException, StateEventHandleError {
        TaskStateEvent taskStateEvent = (TaskStateEvent) stateEvent;
        measureTaskState(taskStateEvent);
        workflowExecuteRunnable.checkTaskInstanceByStateEvent(taskStateEvent);

        Optional<TaskInstance> taskInstanceOptional =
                workflowExecuteRunnable.getTaskInstance(taskStateEvent.getTaskInstanceId());

        TaskInstance task = taskInstanceOptional.orElseThrow(() -> new StateEventHandleError(
                "Cannot find task instance from taskMap by task instance id: " + taskStateEvent.getTaskInstanceId()));

        if (task.getState() == null) {
            throw new StateEventHandleError("Task state event handle error due to task state is null");
        }

        log.info(
                "Handle task instance state event, the current task instance state {} will be changed to {}",
                task.getState().name(), taskStateEvent.getStatus().name());

        Set<Long> completeTaskSet = workflowExecuteRunnable.getCompleteTaskCodes();
        if (task.getState().isFinished()
                && (taskStateEvent.getStatus() != null && taskStateEvent.getStatus().isRunning())) {
            String errorMessage = String.format(
                    "The current task instance state is %s, but the task state event status is %s, so the task state event will be ignored",
                    task.getState().name(),
                    taskStateEvent.getStatus().name());
            log.warn(errorMessage);
            throw new StateEventHandleError(errorMessage);
        }

        if (task.getState().isFinished()) {
            if (completeTaskSet.contains(task.getTaskCode())) {
                log.warn("The task instance is already complete, stateEvent: {}", stateEvent);
                return true;
            }
            workflowExecuteRunnable.taskFinished(task);
            if (task.getTaskGroupId() > 0) {
                log.info("The task instance need to release task Group: {}", task.getTaskGroupId());
                try {
                    workflowExecuteRunnable.releaseTaskGroup(task);
                } catch (Exception e) {
                    throw new StateEventHandleException("Release task group failed", e);
                }
            }
            return true;
        }
        return true;
    }

    @Override
    public StateEventType getEventType() {
        return StateEventType.TASK_STATE_CHANGE;
    }

    private void measureTaskState(TaskStateEvent taskStateEvent) {
        if (taskStateEvent == null || taskStateEvent.getStatus() == null) {
            // the event is broken
            log.warn("The task event is broken..., taskEvent: {}", taskStateEvent);
            return;
        }
        if (taskStateEvent.getStatus().isFinished()) {
            TaskMetrics.incTaskInstanceByState("finish");
        }
        switch (taskStateEvent.getStatus()) {
            case KILL:
                TaskMetrics.incTaskInstanceByState("stop");
                break;
            case SUCCESS:
                TaskMetrics.incTaskInstanceByState("success");
                break;
            case FAILURE:
                TaskMetrics.incTaskInstanceByState("fail");
                break;
            default:
                break;
        }
    }
}
