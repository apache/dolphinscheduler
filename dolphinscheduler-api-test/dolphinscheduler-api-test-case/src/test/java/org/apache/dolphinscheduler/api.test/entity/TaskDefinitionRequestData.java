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

package org.apache.dolphinscheduler.api.test.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskDefinitionRequestData {
    private String code;

    private String delayTime;

    private String description;

    private String environmentCode;

    private String failRetryInterval;

    private String failRetryTimes;

    private String flag;

    private String name;

    private TaskParamsMap taskParams;

    private String taskPriority;

    private String taskType;

    private Integer timeout;

    private String timeoutFlag;

    private String timeoutNotifyStrategy;

    private String workerGroup;

    public void setCode(String code) {
        this.code = code;
    }

    public void setDelayTime(String delayTime) {
        this.delayTime = delayTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
    }

    public void setFailRetryInterval(String failRetryInterval) {
        this.failRetryInterval = failRetryInterval;
    }

    public void setFailRetryTimes(String failRetryTimes) {
        this.failRetryTimes = failRetryTimes;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTaskParams(TaskParamsMap taskParams) {
        this.taskParams = taskParams;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public void setTimeoutFlag(String timeoutFlag) {
        this.timeoutFlag = timeoutFlag;
    }

    public void setTimeoutNotifyStrategy(String timeoutNotifyStrategy) {
        this.timeoutNotifyStrategy = timeoutNotifyStrategy;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public String getCode() {
        return code;
    }

    public String getDelayTime() {
        return delayTime;
    }

    public String getDescription() {
        return description;
    }

    public String getEnvironmentCode() {
        return environmentCode;
    }

    public String getFailRetryInterval() {
        return failRetryInterval;
    }

    public String getFailRetryTimes() {
        return failRetryTimes;
    }

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }

    public TaskParamsMap getTaskParams() {
        return taskParams;
    }

    public String getTaskPriority() {
        return taskPriority;
    }

    public String getTaskType() {
        return taskType;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public String getTimeoutFlag() {
        return timeoutFlag;
    }

    public String getTimeoutNotifyStrategy() {
        return timeoutNotifyStrategy;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }
}
