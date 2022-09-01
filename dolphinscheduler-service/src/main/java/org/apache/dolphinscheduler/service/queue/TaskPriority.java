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

package org.apache.dolphinscheduler.service.queue;

import lombok.Data;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Map;
import java.util.Objects;

@Data
public class TaskPriority implements Comparable<TaskPriority> {

    private int processInstancePriority;

    private int processInstanceId;

    private int taskInstancePriority;

    private int taskInstanceId;

    private TaskExecutionContext taskExecutionContext;

    private String groupName;

    private Map<String, String> context;

    private long checkpoint;

    private int taskGroupPriority;

    private long lastDispatchTime;

    private int dispatchFailedRetryTimes = 0;

    public TaskPriority() {
        this.checkpoint = System.currentTimeMillis();
    }

    public TaskPriority(int processInstancePriority,
                        int processInstanceId,
                        int taskInstancePriority,
                        int taskInstanceId,
                        int taskGroupPriority, String groupName) {
        this.processInstancePriority = processInstancePriority;
        this.processInstanceId = processInstanceId;
        this.taskInstancePriority = taskInstancePriority;
        this.taskInstanceId = taskInstanceId;
        this.taskGroupPriority = taskGroupPriority;
        this.groupName = groupName;
        this.checkpoint = System.currentTimeMillis();
    }

    @Override
    public int compareTo(TaskPriority other) {
        if (this.getProcessInstancePriority() > other.getProcessInstancePriority()) {
            return 1;
        }
        if (this.getProcessInstancePriority() < other.getProcessInstancePriority()) {
            return -1;
        }

        if (this.getProcessInstanceId() > other.getProcessInstanceId()) {
            return 1;
        }
        if (this.getProcessInstanceId() < other.getProcessInstanceId()) {
            return -1;
        }

        if (this.getTaskInstancePriority() > other.getTaskInstancePriority()) {
            return 1;
        }
        if (this.getTaskInstancePriority() < other.getTaskInstancePriority()) {
            return -1;
        }
        if(this.getTaskGroupPriority() != other.getTaskGroupPriority()){
            // larger number, higher priority
            return Constants.OPPOSITE_VALUE * Integer.compare(this.getTaskGroupPriority(), other.getTaskGroupPriority());
        }
        if (this.getTaskInstanceId() > other.getTaskInstanceId()) {
            return 1;
        }
        if (this.getTaskInstanceId() < other.getTaskInstanceId()) {
            return -1;
        }
        if (this.getDispatchFailedRetryTimes() > other.getDispatchFailedRetryTimes()) {
            return -1;
        }
        String thisGroupName = StringUtils.isNotBlank(this.getGroupName()) ? this.getGroupName() : Constants.EMPTY_STRING;
        String otherGroupName = StringUtils.isNotBlank(other.getGroupName()) ? other.getGroupName() : Constants.EMPTY_STRING;
        if(!thisGroupName.equals(otherGroupName)){
            return thisGroupName.compareTo(otherGroupName);
        }
        return Long.compare(this.getCheckpoint(), other.getCheckpoint());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskPriority that = (TaskPriority) o;
        return processInstancePriority == that.processInstancePriority
            && processInstanceId == that.processInstanceId
            && taskInstancePriority == that.taskInstancePriority
            && taskInstanceId == that.taskInstanceId
            && taskGroupPriority == that.taskGroupPriority
            && Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInstancePriority,
            processInstanceId,
            taskInstancePriority,
                taskInstanceId,
            taskGroupPriority,
            groupName);
    }

}
