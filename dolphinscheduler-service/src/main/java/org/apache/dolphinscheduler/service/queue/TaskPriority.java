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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

import lombok.Data;

@Data
public class TaskPriority implements Comparable<TaskPriority> {

    private int workflowInstancePriority;

    private int workflowInstanceId;

    private int taskInstancePriority;

    private int taskId;

    private TaskExecutionContext taskExecutionContext;

    private String groupName;

    private Map<String, String> context;
    private long checkpoint;

    private int taskGroupPriority;

    public TaskPriority() {
        this.checkpoint = System.currentTimeMillis();
    }

    public TaskPriority(int workflowInstancePriority,
                        int workflowInstanceId,
                        int taskInstancePriority,
                        int taskId,
                        int taskGroupPriority, String groupName) {
        this.workflowInstancePriority = workflowInstancePriority;
        this.workflowInstanceId = workflowInstanceId;
        this.taskInstancePriority = taskInstancePriority;
        this.taskId = taskId;
        this.taskGroupPriority = taskGroupPriority;
        this.groupName = groupName;
        this.checkpoint = System.currentTimeMillis();
    }

    @Override
    public int compareTo(TaskPriority other) {
        if (this.getWorkflowInstancePriority() > other.getWorkflowInstancePriority()) {
            return 1;
        }
        if (this.getWorkflowInstancePriority() < other.getWorkflowInstancePriority()) {
            return -1;
        }

        if (this.getWorkflowInstanceId() > other.getWorkflowInstanceId()) {
            return 1;
        }
        if (this.getWorkflowInstanceId() < other.getWorkflowInstanceId()) {
            return -1;
        }

        if (this.getTaskInstancePriority() > other.getTaskInstancePriority()) {
            return 1;
        }
        if (this.getTaskInstancePriority() < other.getTaskInstancePriority()) {
            return -1;
        }
        if (this.getTaskGroupPriority() != other.getTaskGroupPriority()) {
            // larger number, higher priority
            return Constants.OPPOSITE_VALUE
                    * Integer.compare(this.getTaskGroupPriority(), other.getTaskGroupPriority());
        }
        if (this.getTaskId() > other.getTaskId()) {
            return 1;
        }
        if (this.getTaskId() < other.getTaskId()) {
            return -1;
        }
        String thisGroupName =
                StringUtils.isNotBlank(this.getGroupName()) ? this.getGroupName() : Constants.EMPTY_STRING;
        String otherGroupName =
                StringUtils.isNotBlank(other.getGroupName()) ? other.getGroupName() : Constants.EMPTY_STRING;
        if (!thisGroupName.equals(otherGroupName)) {
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
        return workflowInstancePriority == that.workflowInstancePriority
                && workflowInstanceId == that.workflowInstanceId
                && taskInstancePriority == that.taskInstancePriority
                && taskId == that.taskId
                && taskGroupPriority == that.taskGroupPriority
                && Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workflowInstancePriority,
                workflowInstanceId,
                taskInstancePriority,
                taskId,
                taskGroupPriority,
                groupName);
    }

    @Override
    public String toString() {
        return "TaskPriority{"
                + "workflowInstancePriority="
                + workflowInstancePriority
                + ", workflowInstanceId="
                + workflowInstanceId
                + ", taskInstancePriority="
                + taskInstancePriority
                + ", taskId="
                + taskId
                + ", taskExecutionContext="
                + taskExecutionContext
                + ", groupName='"
                + groupName
                + '\''
                + ", context="
                + context
                + ", checkpoint="
                + checkpoint
                + ", taskGroupPriority="
                + taskGroupPriority
                + '}';
    }
}
