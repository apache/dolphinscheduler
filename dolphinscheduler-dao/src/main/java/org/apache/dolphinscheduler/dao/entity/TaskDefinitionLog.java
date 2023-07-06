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

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * task definition log
 */
@TableName("t_ds_task_definition_log")
public class TaskDefinitionLog extends TaskDefinition {

    /**
     * operator user id
     */
    private int operator;

    /**
     * operate time
     */
    private Date operateTime;

    public TaskDefinitionLog() {
        super();
    }

    public TaskDefinitionLog(TaskDefinition taskDefinition) {
        super();
        this.setCode(taskDefinition.getCode());
        this.setVersion(taskDefinition.getVersion());
        this.setName(taskDefinition.getName());
        this.setDescription(taskDefinition.getDescription());
        this.setUserId(taskDefinition.getUserId());
        this.setUserName(taskDefinition.getUserName());
        this.setWorkerGroup(taskDefinition.getWorkerGroup());
        this.setEnvironmentCode(taskDefinition.getEnvironmentCode());
        this.setProjectCode(taskDefinition.getProjectCode());
        this.setProjectName(taskDefinition.getProjectName());
        this.setResourceIds(taskDefinition.getResourceIds());
        this.setTaskParams(taskDefinition.getTaskParams());
        this.setTaskParamList(taskDefinition.getTaskParamList());
        this.setTaskParamMap(taskDefinition.getTaskParamMap());
        this.setTaskPriority(taskDefinition.getTaskPriority());
        this.setTaskExecuteType(taskDefinition.getTaskExecuteType());
        this.setTimeoutNotifyStrategy(taskDefinition.getTimeoutNotifyStrategy());
        this.setTaskType(taskDefinition.getTaskType());
        this.setTimeout(taskDefinition.getTimeout());
        this.setDelayTime(taskDefinition.getDelayTime());
        this.setTimeoutFlag(taskDefinition.getTimeoutFlag());
        this.setUpdateTime(taskDefinition.getUpdateTime());
        this.setCreateTime(taskDefinition.getCreateTime());
        this.setFailRetryInterval(taskDefinition.getFailRetryInterval());
        this.setFailRetryTimes(taskDefinition.getFailRetryTimes());
        this.setFlag(taskDefinition.getFlag());
        this.setIsCache(taskDefinition.getIsCache());
        this.setModifyBy(taskDefinition.getModifyBy());
        this.setCpuQuota(taskDefinition.getCpuQuota());
        this.setMemoryMax(taskDefinition.getMemoryMax());
        this.setTaskExecuteType(taskDefinition.getTaskExecuteType());
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    public Date getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Date operateTime) {
        this.operateTime = operateTime;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
