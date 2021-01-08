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

import java.util.Map;
import java.util.Objects;

/**
 *  task priority info
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
     * groupName
     */
    private String groupName;

    /**
     * context
     */
    private Map<String, String> context;

    public TaskPriority(){}

    public TaskPriority(int processInstancePriority,
                        int processInstanceId,
                        int taskInstancePriority,
                        int taskId, String groupName) {
        this.processInstancePriority = processInstancePriority;
        this.processInstanceId = processInstanceId;
        this.taskInstancePriority = taskInstancePriority;
        this.taskId = taskId;
        this.groupName = groupName;
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

        if (this.getTaskId() > other.getTaskId()) {
            return 1;
        }
        if (this.getTaskId() < other.getTaskId()) {
            return -1;
        }

        return this.getGroupName().compareTo(other.getGroupName());
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
                &&  processInstanceId == that.processInstanceId
                && taskInstancePriority == that.taskInstancePriority
                && taskId == that.taskId
                && Objects.equals(groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processInstancePriority, processInstanceId, taskInstancePriority, taskId, groupName);
    }
}
