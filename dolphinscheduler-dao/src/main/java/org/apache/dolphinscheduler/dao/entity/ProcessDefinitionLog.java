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
 * process definition log
 */
@TableName("t_ds_process_definition_log")
public class ProcessDefinitionLog extends ProcessDefinition {

    /**
     * operator
     */
    private int operator;

    /**
     * operateTime
     */
    private Date operateTime;

    public ProcessDefinitionLog() {
        super();
    }

    public ProcessDefinitionLog(ProcessDefinition processDefinition) {
        this.setCode(processDefinition.getCode());
        this.setName(processDefinition.getName());
        this.setVersion(processDefinition.getVersion());
        this.setReleaseState(processDefinition.getReleaseState());
        this.setProjectCode(processDefinition.getProjectCode());
        this.setDescription(processDefinition.getDescription());
        this.setGlobalParams(processDefinition.getGlobalParams());
        this.setGlobalParamList(processDefinition.getGlobalParamList());
        this.setGlobalParamMap(processDefinition.getGlobalParamMap());
        this.setCreateTime(processDefinition.getCreateTime());
        this.setUpdateTime(processDefinition.getUpdateTime());
        this.setFlag(processDefinition.getFlag());
        this.setUserId(processDefinition.getUserId());
        this.setUserName(processDefinition.getUserName());
        this.setProjectName(processDefinition.getProjectName());
        this.setLocations(processDefinition.getLocations());
        this.setScheduleReleaseState(processDefinition.getScheduleReleaseState());
        this.setTimeout(processDefinition.getTimeout());
        this.setTenantId(processDefinition.getTenantId());
        this.setModifyBy(processDefinition.getModifyBy());
        this.setWarningGroupId(processDefinition.getWarningGroupId());
        this.setExecutionType(processDefinition.getExecutionType());
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

}
