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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * task priority info
 */
public class TaskPriority implements Comparable<TaskPriority> {

    /**
     * processInstancePriority
     */
    private int processInstancePriority;

    /**
     * processInstanceId
     */
    private int processInstanceId;

    /**
     * taskInstancePriority
     */
    private int taskInstancePriority;

    /**
     * taskId
     */
    private int taskId;

    /**
     * taskExecutionContext
     */
    private TaskExecutionContext taskExecutionContext;

    /**
     * groupName
     */
    private String groupName;

    /**
     * context
     */
    private Map<String, String> context;

    /**
     * checkpoint
     */
    private long checkpoint;

    private int taskGroupPriority;

    public TaskPriority() {
        this.checkpoint = System.currentTimeMillis();
    }

    public TaskPriority(int processInstancePriority,
                        int processInstanceId,
                        int taskInstancePriority,
                        int taskId,
                        int taskGroupPriority, String groupName) {
        this.processInstancePriority = processInstancePriority;
        this.processInstanceId = processInstanceId;
        this.taskInstancePriority = taskInstancePriority;
        this.taskId = taskId;
        this.taskGroupPriority = taskGroupPriority;
        this.groupName = groupName;
        this.checkpoint = System.currentTimeMillis();
    }

    public int getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(int processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }

    public int getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(int processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public int getTaskInstancePriority() {
        return taskInstancePriority;
    }

    public void setTaskInstancePriority(int taskInstancePriority) {
        this.taskInstancePriority = taskInstancePriority;
    }

    public int getTaskId() {
        return taskId;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public TaskExecutionContext getTaskExecutionContext() {
        return taskExecutionContext;
    }

    public void setTaskExecutionContext(TaskExecutionContext taskExecutionContext) {
        this.taskExecutionContext = taskExecutionContext;
    }

    public long getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(long checkpoint) {
        this.checkpoint = checkpoint;
    }

    public int getTaskGroupPriority() {
        return taskGroupPriority;
    }

    public void setTaskGroupPriority(int taskGroupPriority) {
        this.taskGroupPriority = taskGroupPriority;
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
        if (this.getTaskId() > other.getTaskId()) {
            return 1;
        }
        if (this.getTaskId() < other.getTaskId()) {
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
            && taskId == that.taskId
            && taskGroupPriority == that.taskGroupPriority
            && Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInstancePriority,
            processInstanceId,
            taskInstancePriority,
            taskId,
            taskGroupPriority,
            groupName);
    }

    @Override
    public String toString() {
        return "TaskPriority{"
            + "processInstancePriority="
            + processInstancePriority
            + ", processInstanceId="
            + processInstanceId
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
