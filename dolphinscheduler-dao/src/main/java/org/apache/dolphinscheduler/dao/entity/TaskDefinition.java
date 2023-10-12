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

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Data;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;

@Data
@TableName("t_ds_task_definition")
public class TaskDefinition {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
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
    @TableField(exist = false)
    private List<Property> taskParamList;

    /**
     * user defined parameter map
     */
    @TableField(exist = false)
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
    @TableField(exist = false)
    private String userName;

    /**
     * project name
     */
    @TableField(exist = false)
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
    @TableField(updateStrategy = FieldStrategy.IGNORED)
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
    @TableField(exist = false)
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

    public TaskDefinition() {
    }

    public TaskDefinition(long code, int version) {
        this.code = code;
        this.version = version;
    }

    public List<Property> getTaskParamList() {
        JsonNode localParams = JSONUtils.parseObject(taskParams).findValue("localParams");
        if (localParams != null) {
            taskParamList = JSONUtils.toList(localParams.toString(), Property.class);
        }

        return taskParamList;
    }

    public Map<String, String> getTaskParamMap() {
        if (taskParamMap == null && !Strings.isNullOrEmpty(taskParams)) {
            JsonNode localParams = JSONUtils.parseObject(taskParams).findValue("localParams");

            // If a jsonNode is null, not only use !=null, but also it should use the isNull method to be estimated.
            if (localParams != null && !localParams.isNull()) {
                List<Property> propList = JSONUtils.toList(localParams.toString(), Property.class);

                if (CollectionUtils.isNotEmpty(propList)) {
                    taskParamMap = new HashMap<>();
                    for (Property property : propList) {
                        taskParamMap.put(property.getProp(), property.getValue());
                    }
                }
            }
        }
        return taskParamMap;
    }

    public String getDependence() {
        return JSONUtils.getNodeString(this.taskParams, Constants.DEPENDENCE);
    }

    public Integer getCpuQuota() {
        return cpuQuota == null ? -1 : cpuQuota;
    }

    public Integer getMemoryMax() {
        return memoryMax == null ? -1 : memoryMax;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        TaskDefinition that = (TaskDefinition) o;
        return failRetryTimes == that.failRetryTimes
                && failRetryInterval == that.failRetryInterval
                && timeout == that.timeout
                && delayTime == that.delayTime
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(taskType, that.taskType)
                && Objects.equals(taskParams, that.taskParams)
                && flag == that.flag
                && isCache == that.isCache
                && taskPriority == that.taskPriority
                && Objects.equals(workerGroup, that.workerGroup)
                && timeoutFlag == that.timeoutFlag
                && timeoutNotifyStrategy == that.timeoutNotifyStrategy
                && (Objects.equals(resourceIds, that.resourceIds)
                        || ("".equals(resourceIds) && that.resourceIds == null)
                        || ("".equals(that.resourceIds) && resourceIds == null))
                && environmentCode == that.environmentCode
                && taskGroupId == that.taskGroupId
                && taskGroupPriority == that.taskGroupPriority
                && Objects.equals(cpuQuota, that.cpuQuota)
                && Objects.equals(memoryMax, that.memoryMax)
                && Objects.equals(taskExecuteType, that.taskExecuteType);
    }
}
