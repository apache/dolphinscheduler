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

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WorkFlowRunRequestData {

    private String processDefinitionCode;

    private String startEndTime;

    private String failureStrategy;

    private String warningType;

    private String warningGroupId;

    private String execType;

    private String startNodeList;

    private String taskDependType;

    private String dependentMode;

    private String runMode;

    private String processInstancePriority;

    private String workerGroup;

    private String environmentCode;

    private String startParams;

    private String expectedParallelismNumber;

    private Integer dryRun;

    private String scheduleTime;

    public String getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public String getStartEndTime() {
        return startEndTime;
    }

    public String getFailureStrategy() {
        return failureStrategy;
    }

    public String getWarningType() {
        return warningType;
    }

    public String getWarningGroupId() {
        return warningGroupId;
    }

    public String getExecType() {
        return execType;
    }

    public String getStartNodeList() {
        return startNodeList;
    }

    public String getTaskDependType() {
        return taskDependType;
    }

    public String getDependentMode() {
        return dependentMode;
    }

    public String getRunMode() {
        return runMode;
    }

    public String getProcessInstancePriority() {
        return processInstancePriority;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public String getEnvironmentCode() {
        return environmentCode;
    }

    public String getStartParams() {
        return startParams;
    }

    public String getExpectedParallelismNumber() {
        return expectedParallelismNumber;
    }

    public Integer getDryRun() {
        return dryRun;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setProcessDefinitionCode(String processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public void setStartEndTime(String startEndTime) {
        this.startEndTime = startEndTime;
    }

    public void setFailureStrategy(String failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public void setWarningType(String warningType) {
        this.warningType = warningType;
    }

    public void setWarningGroupId(String warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public void setExecType(String execType) {
        this.execType = execType;
    }

    public void setStartNodeList(String startNodeList) {
        this.startNodeList = startNodeList;
    }

    public void setTaskDependType(String taskDependType) {
        this.taskDependType = taskDependType;
    }

    public void setDependentMode(String dependentMode) {
        this.dependentMode = dependentMode;
    }

    public void setRunMode(String runMode) {
        this.runMode = runMode;
    }

    public void setProcessInstancePriority(String processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public void setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
    }

    public void setStartParams(String startParams) {
        this.startParams = startParams;
    }

    public void setExpectedParallelismNumber(String expectedParallelismNumber) {
        this.expectedParallelismNumber = expectedParallelismNumber;
    }

    public void setDryRun(Integer dryRun) {
        this.dryRun = dryRun;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }




}
