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

package org.apache.dolphinscheduler.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * Task Group Queue
 */
@TableName("t_ds_task_group_queue")
public class TaskGroupQueue implements Serializable {
    /**
     * key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * taskIntanceid
     */
    private int taskId;
    /**
     * TaskInstance name
     */
    private String taskName;
    /**
     * project name
     */
    @TableField(exist = false)
    private String projectName;
    /**
     * project code
     */
    @TableField(exist = false)
    private String projectCode;
    /**
     * process instance name
     */
    @TableField(exist = false)
    private String processInstanceName;
    /**
     * taskGroup id
     */
    private int groupId;
    /**
     * processInstace id
     */
    private int processId;
    /**
     * the priority of task instance
     */
    private int priority;
    /**
     * is force start
     * 0 NO ,1 YES
     */
    private int forceStart;
    /**
     * ready to get the queue by other task finish
     * 0 NO ,1 YES
     */
    private int inQueue;
    /**
     * -1: waiting  1: running  2: finished
     */
    private TaskGroupQueueStatus status;
    /**
     * create time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * update time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public TaskGroupQueue() {

    }

    public TaskGroupQueue(int taskId, String taskName, int groupId, int processId, int priority, TaskGroupQueueStatus status) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.groupId = groupId;
        this.processId = processId;
        this.priority = priority;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "TaskGroupQueue{"
            + "id=" + id
            + ", taskId=" + taskId
            + ", taskName='" + taskName + '\''
            + ", groupId=" + groupId
            + ", processId=" + processId
            + ", priority=" + priority
            + ", status=" + status
            + ", createTime=" + createTime
            + ", updateTime=" + updateTime
            + '}';
    }

    public TaskGroupQueueStatus getStatus() {
        return status;
    }

    public void setStatus(TaskGroupQueueStatus status) {
        this.status = status;
    }

    public int getForceStart() {
        return forceStart;
    }

    public void setForceStart(int forceStart) {
        this.forceStart = forceStart;
    }

    public int getInQueue() {
        return inQueue;
    }

    public void setInQueue(int inQueue) {
        this.inQueue = inQueue;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProcessInstanceName() {
        return processInstanceName;
    }

    public void setProcessInstanceName(String processInstanceName) {
        this.processInstanceName = processInstanceName;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
}
