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

import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class PriorityDelayTaskExecuteRunnable extends BaseTaskExecuteRunnable implements Delayed {

    public PriorityDelayTaskExecuteRunnable(ProcessInstance workflowInstance,
                                            TaskInstance taskInstance,
                                            TaskExecutionContext taskExecutionContext) {
        super(workflowInstance, taskInstance, taskExecutionContext);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(
                DateUtils.getRemainTime(taskExecutionContext.getFirstSubmitTime(),
                        taskExecutionContext.getDelayTime() * 60L),
                TimeUnit.SECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) {
            return 1;
        }
        int delayTimeCompareResult =
                Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
        if (delayTimeCompareResult != 0) {
            return delayTimeCompareResult;
        }
        PriorityDelayTaskExecuteRunnable other = (PriorityDelayTaskExecuteRunnable) o;
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
        long workflowInstanceIdCompareResult = workflowInstance.getId().compareTo(other.getWorkflowInstance().getId());
        if (workflowInstanceIdCompareResult != 0) {
            return workflowInstancePriorityCompareResult;
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
        return taskInstance.getId().compareTo(other.getTaskInstance().getId());
    }

}
