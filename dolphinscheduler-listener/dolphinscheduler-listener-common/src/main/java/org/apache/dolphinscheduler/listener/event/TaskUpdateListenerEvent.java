/*
 * Licensed to Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Apache Software Foundation (ASF) licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.dolphinscheduler.listener.event;

import java.util.List;
import java.util.Map;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.dao.entity.TaskDefinitionLog;
import org.apache.dolphinscheduler.listener.enums.ListenerEventType;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class TaskUpdateListenerEvent extends ListenerEvent {

    /**
     * id
     */
    private Integer id;

    /**
     * code
     */
    private long code;

    /**
     * name
     */
    private String name;

    /**
     * version
     */
    private int version;

    /**
     * description
     */
    private String description;

    /**
     * project code
     */
    private long projectCode;

    /**
     * task user id
     */
    private int userId;

    /**
     * task type
     */
    private String taskType;

    /**
     * user defined parameters
     */
    private String taskParams;

    /**
     * user defined parameter list
     */
    private List<Property> taskParamList;

    /**
     * user defined parameter map
     */
    private Map<String, String> taskParamMap;

    /**
     * task is valid: yes/no
     */
    private Flag flag;

    /**
     * task is cache: yes/no
     */
    private Flag isCache;

    /**
     * task priority
     */
    private Priority taskPriority;

    /**
     * user name
     */
    private String userName;

    /**
     * project name
     */
    private String projectName;

    /**
     * worker group
     */
    private String workerGroup;

    /**
     * environment code
     */
    private long environmentCode;

    /**
     * fail retry times
     */
    private int failRetryTimes;

    /**
     * fail retry interval
     */
    private int failRetryInterval;

    /**
     * timeout flag
     */
    private TimeoutFlag timeoutFlag;

    /**
     * timeout notify strategy
     */
    private TaskTimeoutStrategy timeoutNotifyStrategy;

    /**
     * task warning time out. unit: minute
     */
    private int timeout;

    /**
     * delay execution time.
     */
    private int delayTime;

    /**
     * resource ids
     */
    private String resourceIds;

    /**
     * create time
     */
    private Date createTime;

    /**
     * update time
     */
    private Date updateTime;

    /**
     * modify user name
     */
    private String modifyBy;

    /**
     * task group id
     */
    private int taskGroupId;
    /**
     * task group id
     */
    private int taskGroupPriority;

    /**
     * cpu quota
     */
    private Integer cpuQuota;

    /**
     * max memory
     */
    private Integer memoryMax;

    /**
     * task execute type
     */
    private TaskExecuteType taskExecuteType;

    /**
     * operator user id
     */
    private int operator;

    /**
     * operate time
     */
    private Date operateTime;

    public TaskUpdateListenerEvent(TaskDefinitionLog taskDefinitionLog){
        this.setCode(taskDefinitionLog.getCode());
        this.setVersion(taskDefinitionLog.getVersion());
        this.setName(taskDefinitionLog.getName());
        this.setDescription(taskDefinitionLog.getDescription());
        this.setUserId(taskDefinitionLog.getUserId());
        this.setUserName(taskDefinitionLog.getUserName());
        this.setWorkerGroup(taskDefinitionLog.getWorkerGroup());
        this.setEnvironmentCode(taskDefinitionLog.getEnvironmentCode());
        this.setProjectCode(taskDefinitionLog.getProjectCode());
        this.setProjectName(taskDefinitionLog.getProjectName());
        this.setResourceIds(taskDefinitionLog.getResourceIds());
        this.setTaskParams(taskDefinitionLog.getTaskParams());
        this.setTaskParamList(taskDefinitionLog.getTaskParamList());
        this.setTaskParamMap(taskDefinitionLog.getTaskParamMap());
        this.setTaskPriority(taskDefinitionLog.getTaskPriority());
        this.setTaskExecuteType(taskDefinitionLog.getTaskExecuteType());
        this.setTimeoutNotifyStrategy(taskDefinitionLog.getTimeoutNotifyStrategy());
        this.setTaskType(taskDefinitionLog.getTaskType());
        this.setTimeout(taskDefinitionLog.getTimeout());
        this.setDelayTime(taskDefinitionLog.getDelayTime());
        this.setTimeoutFlag(taskDefinitionLog.getTimeoutFlag());
        this.setUpdateTime(taskDefinitionLog.getUpdateTime());
        this.setCreateTime(taskDefinitionLog.getCreateTime());
        this.setFailRetryInterval(taskDefinitionLog.getFailRetryInterval());
        this.setFailRetryTimes(taskDefinitionLog.getFailRetryTimes());
        this.setFlag(taskDefinitionLog.getFlag());
        this.setIsCache(taskDefinitionLog.getIsCache());
        this.setModifyBy(taskDefinitionLog.getModifyBy());
        this.setCpuQuota(taskDefinitionLog.getCpuQuota());
        this.setMemoryMax(taskDefinitionLog.getMemoryMax());
        this.setTaskExecuteType(taskDefinitionLog.getTaskExecuteType());
        this.setOperator(taskDefinitionLog.getOperator());
        this.setOperateTime(taskDefinitionLog.getOperateTime());
        this.setListenerEventType(ListenerEventType.TASK_UPDATE);
    }
}
