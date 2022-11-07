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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelation;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author fanwanlong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDefinitionVo {

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
    @JsonDeserialize(using = JSONUtils.JsonDataDeserializer.class)
    @JsonSerialize(using = JSONUtils.JsonDataSerializer.class)
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
     * process task related list
     */
    private List<ProcessTaskRelation> processTaskRelationList;

    public static TaskDefinitionVo fromTaskDefinition(TaskDefinition taskDefinition) {
        return TaskDefinitionVo.builder()
                .id(taskDefinition.getId())
                .code(taskDefinition.getCode())
                .name(taskDefinition.getName())
                .version(taskDefinition.getVersion())
                .description(taskDefinition.getDescription())
                .projectCode(taskDefinition.getProjectCode())
                .userId(taskDefinition.getUserId())
                .taskType(taskDefinition.getTaskType())
                .taskParams(taskDefinition.getTaskParams())
                .taskParamList(taskDefinition.getTaskParamList())
                .taskParamMap(taskDefinition.getTaskParamMap())
                .flag(taskDefinition.getFlag())
                .taskPriority(taskDefinition.getTaskPriority())
                .userName(taskDefinition.getUserName())
                .projectName(taskDefinition.getProjectName())
                .workerGroup(taskDefinition.getWorkerGroup())
                .environmentCode(taskDefinition.getEnvironmentCode())
                .failRetryTimes(taskDefinition.getFailRetryTimes())
                .failRetryInterval(taskDefinition.getFailRetryInterval())
                .timeoutFlag(taskDefinition.getTimeoutFlag())
                .timeoutNotifyStrategy(taskDefinition.getTimeoutNotifyStrategy())
                .timeout(taskDefinition.getTimeout())
                .delayTime(taskDefinition.getDelayTime())
                .resourceIds(taskDefinition.getResourceIds())
                .createTime(taskDefinition.getCreateTime())
                .updateTime(taskDefinition.getUpdateTime())
                .modifyBy(taskDefinition.getModifyBy())
                .taskGroupId(taskDefinition.getTaskGroupId())
                .taskGroupPriority(taskDefinition.getTaskGroupPriority())
                .cpuQuota(taskDefinition.getCpuQuota())
                .memoryMax(taskDefinition.getMemoryMax())
                .taskExecuteType(taskDefinition.getTaskExecuteType())
                .build();
    }

}
