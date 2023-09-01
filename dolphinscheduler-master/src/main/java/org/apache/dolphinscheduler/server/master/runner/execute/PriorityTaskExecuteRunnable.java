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

package org.apache.dolphinscheduler.server.master.runner.execute;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.jetbrains.annotations.NotNull;

public abstract class PriorityTaskExecuteRunnable implements TaskExecuteRunnable, Comparable<TaskExecuteRunnable> {

    private final ProcessInstance workflowInstance;
    private final TaskInstance taskInstance;
    private final TaskExecutionContext taskExecutionContext;

    public PriorityTaskExecuteRunnable(ProcessInstance workflowInstance,
                                       TaskInstance taskInstance,
                                       TaskExecutionContext taskExecutionContext) {
        this.taskInstance = checkNotNull(taskInstance);
        this.workflowInstance = checkNotNull(workflowInstance);
        this.taskExecutionContext = checkNotNull(taskExecutionContext);
    }

    @Override
    public ProcessInstance getWorkflowInstance() {
        return workflowInstance;
    }

    @Override
    public TaskInstance getTaskInstance() {
        return taskInstance;
    }

    @Override
    public TaskExecutionContext getTaskExecutionContext() {
        return taskExecutionContext;
    }

    @Override
    public int compareTo(@NotNull TaskExecuteRunnable other) {
        // the smaller dispatch fail times, the higher priority
        int dispatchFailTimesCompareResult = taskExecutionContext.getDispatchFailTimes()
                - other.getTaskExecutionContext().getDispatchFailTimes();
        if (dispatchFailTimesCompareResult != 0) {
            return dispatchFailTimesCompareResult;
        }

        int workflowInstancePriorityCompareResult = workflowInstance.getProcessInstancePriority().getCode()
                - other.getWorkflowInstance().getProcessInstancePriority().getCode();
        if (workflowInstancePriorityCompareResult != 0) {
            return workflowInstancePriorityCompareResult;
        }
        int workflowInstanceIdCompareResult = workflowInstance.getId() - other.getWorkflowInstance().getId();
        if (workflowInstanceIdCompareResult != 0) {
            return workflowInstanceIdCompareResult;
        }
        int taskInstancePriorityCompareResult = taskInstance.getTaskInstancePriority().getCode()
                - other.getTaskInstance().getTaskInstancePriority().getCode();
        if (taskInstancePriorityCompareResult != 0) {
            return taskInstancePriorityCompareResult;
        }
        // larger number, higher priority
        int taskGroupPriorityCompareResult =
                taskInstance.getTaskGroupPriority() - other.getTaskInstance().getTaskGroupPriority();
        if (taskGroupPriorityCompareResult != 0) {
            return -taskGroupPriorityCompareResult;
        }
        // The task instance shouldn't be equals
        return taskInstance.getId() - other.getTaskInstance().getId();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PriorityTaskExecuteRunnable) {
            PriorityTaskExecuteRunnable other = (PriorityTaskExecuteRunnable) obj;
            return compareTo(other) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return taskInstance.getId();
    }

}
