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

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

public abstract class BaseTaskExecuteRunnable implements TaskExecuteRunnable {

    protected final ProcessInstance workflowInstance;
    protected final TaskInstance taskInstance;
    protected final TaskExecutionContext taskExecutionContext;

    public BaseTaskExecuteRunnable(ProcessInstance workflowInstance,
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
    public int compareTo(TaskExecuteRunnable other) {
        if (other == null) {
            return 1;
        }
        int workflowInstancePriorityCompareResult = workflowInstance.getProcessInstancePriority().getCode() -
                other.getWorkflowInstance().getProcessInstancePriority().getCode();
        if (workflowInstancePriorityCompareResult != 0) {
            return workflowInstancePriorityCompareResult;
        }

        // smaller number, higher priority
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
        // earlier submit time, higher priority
        return taskInstance.getFirstSubmitTime().compareTo(other.getTaskInstance().getFirstSubmitTime());
    }

}
