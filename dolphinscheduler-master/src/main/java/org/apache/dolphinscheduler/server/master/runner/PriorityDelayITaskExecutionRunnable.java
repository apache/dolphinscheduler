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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class PriorityDelayITaskExecutionRunnable extends BaseITaskExecutionRunnable implements Delayed {

    public PriorityDelayITaskExecutionRunnable(TaskExecutionRunnableContext taskExecutionRunnableContext) {
        super(taskExecutionRunnableContext);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(
                DateUtils.getRemainTime(taskExecutionRunnableContext.getTaskExecutionContext().getFirstSubmitTime(),
                        taskExecutionRunnableContext.getTaskExecutionContext().getDelayTime() * 60L),
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
        PriorityDelayITaskExecutionRunnable other = (PriorityDelayITaskExecutionRunnable) o;
        // the smaller dispatch fail times, the higher priority
        int dispatchFailTimesCompareResult =
                taskExecutionRunnableContext.getTaskExecutionContext().getDispatchFailTimes()
                        - other.getTaskExecutionRunnableContext().getTaskExecutionContext().getDispatchFailTimes();
        if (dispatchFailTimesCompareResult != 0) {
            return dispatchFailTimesCompareResult;
        }
        int workflowInstancePriorityCompareResult = taskExecutionRunnableContext.getWorkflowInstance()
                .getProcessInstancePriority().getCode()
                - other.getTaskExecutionRunnableContext().getWorkflowInstance().getProcessInstancePriority().getCode();
        if (workflowInstancePriorityCompareResult != 0) {
            return workflowInstancePriorityCompareResult;
        }
        long workflowInstanceIdCompareResult = taskExecutionRunnableContext.getWorkflowInstance().getId()
                .compareTo(other.getTaskExecutionRunnableContext().getWorkflowInstance().getId());
        if (workflowInstanceIdCompareResult != 0) {
            return workflowInstancePriorityCompareResult;
        }
        int taskInstancePriorityCompareResult =
                taskExecutionRunnableContext.getTaskInstance().getTaskInstancePriority().getCode()
                        - other.getTaskExecutionRunnableContext().getTaskInstance().getTaskInstancePriority().getCode();
        if (taskInstancePriorityCompareResult != 0) {
            return taskInstancePriorityCompareResult;
        }
        // larger number, higher priority
        int taskGroupPriorityCompareResult =
                taskExecutionRunnableContext.getTaskInstance().getTaskGroupPriority()
                        - other.getTaskExecutionRunnableContext().getTaskInstance().getTaskGroupPriority();
        if (taskGroupPriorityCompareResult != 0) {
            return -taskGroupPriorityCompareResult;
        }
        // The task instance shouldn't be equals
        return taskExecutionRunnableContext.getTaskInstance().getId()
                .compareTo(other.getTaskExecutionRunnableContext().getTaskInstance().getId());
    }

}
