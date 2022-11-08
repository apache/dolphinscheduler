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

package org.apache.dolphinscheduler.api.vo;

import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;

import java.util.List;

import lombok.Data;

/**
 * @author fanwanlong
 */
@Data
public class TaskDefinitionVo extends TaskDefinition {

    /**
     * process task related list
     */
    private List<ProcessTaskRelation> processTaskRelationList;

    public TaskDefinitionVo() {
    }

    public TaskDefinitionVo(List<ProcessTaskRelation> processTaskRelationList) {
        this.processTaskRelationList = processTaskRelationList;
    }

    public static TaskDefinitionVo fromTaskDefinition(TaskDefinition taskDefinition) {
        TaskDefinitionVo taskDefinitionVo = new TaskDefinitionVo();
        taskDefinitionVo.setId(taskDefinition.getId());
        taskDefinitionVo.setCode(taskDefinition.getCode());
        taskDefinitionVo.setName(taskDefinition.getName());
        taskDefinitionVo.setVersion(taskDefinition.getVersion());
        taskDefinitionVo.setDescription(taskDefinition.getDescription());
        taskDefinitionVo.setProjectCode(taskDefinition.getProjectCode());
        taskDefinitionVo.setUserId(taskDefinition.getUserId());
        taskDefinitionVo.setTaskType(taskDefinition.getTaskType());
        taskDefinitionVo.setTaskParams(taskDefinition.getTaskParams());
        taskDefinitionVo.setTaskParamList(taskDefinition.getTaskParamList());
        taskDefinitionVo.setTaskParamMap(taskDefinition.getTaskParamMap());
        taskDefinitionVo.setFlag(taskDefinition.getFlag());
        taskDefinitionVo.setTaskPriority(taskDefinition.getTaskPriority());
        taskDefinitionVo.setUserName(taskDefinition.getUserName());
        taskDefinitionVo.setProjectName(taskDefinition.getProjectName());
        taskDefinitionVo.setWorkerGroup(taskDefinition.getWorkerGroup());
        taskDefinitionVo.setEnvironmentCode(taskDefinition.getEnvironmentCode());
        taskDefinitionVo.setFailRetryTimes(taskDefinition.getFailRetryTimes());
        taskDefinitionVo.setFailRetryInterval(taskDefinition.getFailRetryInterval());
        taskDefinitionVo.setTimeoutFlag(taskDefinition.getTimeoutFlag());
        taskDefinitionVo.setTimeoutNotifyStrategy(taskDefinition.getTimeoutNotifyStrategy());
        taskDefinitionVo.setTimeout(taskDefinition.getTimeout());
        taskDefinitionVo.setDelayTime(taskDefinition.getDelayTime());
        taskDefinitionVo.setResourceIds(taskDefinition.getResourceIds());
        taskDefinitionVo.setCreateTime(taskDefinition.getCreateTime());
        taskDefinitionVo.setUpdateTime(taskDefinition.getUpdateTime());
        taskDefinitionVo.setModifyBy(taskDefinition.getModifyBy());
        taskDefinitionVo.setTaskGroupId(taskDefinition.getTaskGroupId());
        taskDefinitionVo.setTaskGroupPriority(taskDefinition.getTaskGroupPriority());
        taskDefinitionVo.setCpuQuota(taskDefinition.getCpuQuota());
        taskDefinitionVo.setMemoryMax(taskDefinition.getMemoryMax());
        taskDefinitionVo.setTaskExecuteType(taskDefinition.getTaskExecuteType());
        return taskDefinitionVo;
    }

}
