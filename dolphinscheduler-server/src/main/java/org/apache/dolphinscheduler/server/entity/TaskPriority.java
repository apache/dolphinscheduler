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

package org.apache.dolphinscheduler.server.entity;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 *  task priority info
 */
public class TaskPriority {

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
     *   ${processInstancePriority}_${processInstanceId}_${taskInstancePriority}_${taskId}_${groupName}
     */
    private String taskPriorityInfo;

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
        this.taskPriorityInfo = this.processInstancePriority +
                UNDERLINE +
                this.processInstanceId +
                UNDERLINE +
                this.taskInstancePriority +
                UNDERLINE +
                this.taskId +
                UNDERLINE +
                this.groupName;
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

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTaskPriorityInfo() {
        return taskPriorityInfo;
    }

    public void setTaskPriorityInfo(String taskPriorityInfo) {
        this.taskPriorityInfo = taskPriorityInfo;
    }

    /**
     * taskPriorityInfo convert taskPriority
     *
     * @param taskPriorityInfo taskPriorityInfo
     * @return TaskPriority
     */
    public static TaskPriority of(String taskPriorityInfo){
        String[] parts = taskPriorityInfo.split(UNDERLINE);

        if (parts.length != 5) {
            throw new IllegalArgumentException(String.format("TaskPriority : %s illegal.", taskPriorityInfo));
        }
        TaskPriority taskPriority = new TaskPriority(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]),
                parts[4]);
        return taskPriority;
    }
}
