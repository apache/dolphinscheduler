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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.common.enums.TimeoutFlag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * task definition
 */
@TableName("t_ds_task_definition")
public class TaskDefinition {

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;

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
     * user define parameter map
     */
    @TableField(exist = false)
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

    public TaskDefinition() {
    }

    public TaskDefinition(long code, int version) {
        this.code = code;
        this.version = version;
    }

    public int getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(int taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Flag getFlag() {
        return flag;
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getTaskParams() {
        return taskParams;
    }

    public void setTaskParams(String taskParams) {
        this.taskParams = taskParams;
    }

    public List<Property> getTaskParamList() {
        JsonNode localParams = JSONUtils.parseObject(taskParams).findValue("localParams");
        if (localParams != null) {
            taskParamList = JSONUtils.toList(localParams.toString(), Property.class);
        }

        return taskParamList;
    }

    public void setTaskParamList(List<Property> taskParamList) {
        this.taskParamList = taskParamList;
    }

    public void setTaskParamMap(Map<String, String> taskParamMap) {
        this.taskParamMap = taskParamMap;
    }

    public Map<String, String> getTaskParamMap() {
        if (taskParamMap == null && StringUtils.isNotEmpty(taskParams)) {
            JsonNode localParams = JSONUtils.parseObject(taskParams).findValue("localParams");

            //If a jsonNode is null, not only use !=null, but also it should use the isNull method to be estimated.
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

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(long projectCode) {
        this.projectCode = projectCode;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public Priority getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(Priority taskPriority) {
        this.taskPriority = taskPriority;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public int getFailRetryTimes() {
        return failRetryTimes;
    }

    public void setFailRetryTimes(int failRetryTimes) {
        this.failRetryTimes = failRetryTimes;
    }

    public int getFailRetryInterval() {
        return failRetryInterval;
    }

    public void setFailRetryInterval(int failRetryInterval) {
        this.failRetryInterval = failRetryInterval;
    }

    public TaskTimeoutStrategy getTimeoutNotifyStrategy() {
        return timeoutNotifyStrategy;
    }

    public void setTimeoutNotifyStrategy(TaskTimeoutStrategy timeoutNotifyStrategy) {
        this.timeoutNotifyStrategy = timeoutNotifyStrategy;
    }

    public TimeoutFlag getTimeoutFlag() {
        return timeoutFlag;
    }

    public void setTimeoutFlag(TimeoutFlag timeoutFlag) {
        this.timeoutFlag = timeoutFlag;
    }

    public String getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(String resourceIds) {
        this.resourceIds = resourceIds;
    }

    public int getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(int delayTime) {
        this.delayTime = delayTime;
    }

    public String getDependence() {
        return JSONUtils.getNodeString(this.taskParams, Constants.DEPENDENCE);
    }

    public String getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }

    public long getEnvironmentCode() {
        return this.environmentCode;
    }

    public void setEnvironmentCode(long environmentCode) {
        this.environmentCode = environmentCode;
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
            && taskPriority == that.taskPriority
            && Objects.equals(workerGroup, that.workerGroup)
            && timeoutFlag == that.timeoutFlag
            && timeoutNotifyStrategy == that.timeoutNotifyStrategy
            && (Objects.equals(resourceIds, that.resourceIds)
            || (StringUtils.EMPTY.equals(resourceIds) && that.resourceIds == null)
            || (StringUtils.EMPTY.equals(that.resourceIds) && resourceIds == null))
            && environmentCode == that.environmentCode
            && taskGroupId == that.taskGroupId
            && taskGroupPriority == that.taskGroupPriority;
    }

    @Override
    public String toString() {
        return "TaskDefinition{"
                + "id=" + id
                + ", code=" + code
                + ", name='" + name + '\''
                + ", version=" + version
                + ", description='" + description + '\''
                + ", projectCode=" + projectCode
                + ", userId=" + userId
                + ", taskType=" + taskType
                + ", taskParams='" + taskParams + '\''
                + ", taskParamList=" + taskParamList
                + ", taskParamMap=" + taskParamMap
                + ", flag=" + flag
                + ", taskPriority=" + taskPriority
                + ", userName='" + userName + '\''
                + ", projectName='" + projectName + '\''
                + ", workerGroup='" + workerGroup + '\''
                + ", failRetryTimes=" + failRetryTimes
                + ", environmentCode='" + environmentCode + '\''
                + ", taskGroupId='" + taskGroupId + '\''
                + ", taskGroupPriority='" + taskGroupPriority + '\''
                + ", failRetryInterval=" + failRetryInterval
                + ", timeoutFlag=" + timeoutFlag
                + ", timeoutNotifyStrategy=" + timeoutNotifyStrategy
                + ", timeout=" + timeout
                + ", delayTime=" + delayTime
                + ", resourceIds='" + resourceIds + '\''
                + ", createTime=" + createTime
                + ", updateTime=" + updateTime
                + '}';
    }

    public int getTaskGroupPriority() {
        return taskGroupPriority;
    }

    public void setTaskGroupPriority(int taskGroupPriority) {
        this.taskGroupPriority = taskGroupPriority;
    }
}
