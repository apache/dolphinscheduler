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

import org.apache.dolphinscheduler.common.enums.TaskGroupQueueStatus;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@Data
@TableName("t_ds_task_group_queue")
public class TaskGroupQueue implements Serializable {

    /**
     * key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
     * taskInstanceId
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
     * processInstance id
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
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;

    public TaskGroupQueue() {

    }

    public TaskGroupQueue(int taskId, String taskName, int groupId, int processId, int priority,
                          TaskGroupQueueStatus status) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.groupId = groupId;
        this.processId = processId;
        this.priority = priority;
        this.status = status;
    }
}
