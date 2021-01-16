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
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
    private TaskType taskType;

    /**
     * user defined parameters
     */
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
     * fail retry times
     */
    private int failRetryTimes;

    /**
     * fail retry interval
     */
    private int failRetryInterval;

    /**
     * timeout notify strategy
     */
    private TaskTimeoutStrategy TaskTimeoutStrategy;

    /**
     * task warning time out. unit: minute
     */
    private int timeout;

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
        if (taskParams == null) {
            this.taskParamList = new ArrayList<>();
        } else {
            this.taskParamList = JSONUtils.toList(taskParams, Property.class);
        }
        this.taskParams = taskParams;
    }

    public List<Property> getTaskParamList() {
        return taskParamList;
    }

    public void setTaskParamList(List<Property> taskParamList) {
        this.taskParams = JSONUtils.toJsonString(taskParamList);
        this.taskParamList = taskParamList;
    }

    public Map<String, String> getTaskParamMap() {
        if (taskParamMap == null && StringUtils.isNotEmpty(taskParams)) {
            List<Property> propList = JSONUtils.toList(taskParams, Property.class);
            taskParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }

        return taskParamMap;
    }

    public void setTaskParamMap(Map<String, String> taskParamMap) {
        this.taskParamMap = taskParamMap;
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

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
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

    public TaskTimeoutStrategy getTaskTimeoutStrategy() {
        return TaskTimeoutStrategy;
    }

    public void setTaskTimeoutStrategy(TaskTimeoutStrategy taskTimeoutStrategy) {
        TaskTimeoutStrategy = taskTimeoutStrategy;
    }
}
