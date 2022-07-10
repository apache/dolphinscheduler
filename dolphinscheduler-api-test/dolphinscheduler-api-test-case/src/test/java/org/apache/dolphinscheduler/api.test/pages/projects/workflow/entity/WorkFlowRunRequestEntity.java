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

package org.apache.dolphinscheduler.api.test.pages.projects.workflow.entity;

import org.apache.dolphinscheduler.api.test.base.AbstractBaseEntity;
import org.apache.dolphinscheduler.api.test.utils.enums.ComplementDependentMode;
import org.apache.dolphinscheduler.api.test.utils.enums.ExecCommandType;
import org.apache.dolphinscheduler.api.test.utils.enums.FailureStrategy;
import org.apache.dolphinscheduler.api.test.utils.enums.Priority;
import org.apache.dolphinscheduler.api.test.utils.enums.RunMode;
import org.apache.dolphinscheduler.api.test.utils.enums.TaskDependType;
import org.apache.dolphinscheduler.api.test.utils.enums.WarningType;

public class WorkFlowRunRequestEntity extends AbstractBaseEntity {

    private String processDefinitionCode;

    private String scheduleTime;

    private FailureStrategy failureStrategy;

    private String startNodeList;

    private TaskDependType taskDependType;

    private ExecCommandType execType;

    private WarningType warningType;

    private String warningGroupId;

    private RunMode runMode;

    private Priority processInstancePriority;

    private String workerGroup;

    private String environmentCode;

    private String startParams;

    private String expectedParallelismNumber;

    private int dryRun;

    private ComplementDependentMode complementDependentMode;

    public String getProcessDefinitionCode() {
        return processDefinitionCode;
    }

    public void setProcessDefinitionCode(String processDefinitionCode) {
        this.processDefinitionCode = processDefinitionCode;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public FailureStrategy getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(FailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public String getStartNodeList() {
        return startNodeList;
    }

    public void setStartNodeList(String startNodeList) {
        this.startNodeList = startNodeList;
    }

    public TaskDependType getTaskDependType() {
        return taskDependType;
    }

    public void setTaskDependType(TaskDependType taskDependType) {
        this.taskDependType = taskDependType;
    }

    public ExecCommandType getExecType() {
        return execType;
    }

    public void setExecType(ExecCommandType execType) {
        this.execType = execType;
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public void setWarningType(WarningType warningType) {
        this.warningType = warningType;
    }

    public String getWarningGroupId() {
        return warningGroupId;
    }

    public void setWarningGroupId(String warningGroupId) {
        this.warningGroupId = warningGroupId;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public void setRunMode(RunMode runMode) {
        this.runMode = runMode;
    }

    public Priority getProcessInstancePriority() {
        return processInstancePriority;
    }

    public void setProcessInstancePriority(Priority processInstancePriority) {
        this.processInstancePriority = processInstancePriority;
    }

    public String getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(String workerGroup) {
        this.workerGroup = workerGroup;
    }

    public String getEnvironmentCode() {
        return environmentCode;
    }

    public void setEnvironmentCode(String environmentCode) {
        this.environmentCode = environmentCode;
    }

    public String getStartParams() {
        return startParams;
    }

    public void setStartParams(String startParams) {
        this.startParams = startParams;
    }

    public String getExpectedParallelismNumber() {
        return expectedParallelismNumber;
    }

    public void setExpectedParallelismNumber(String expectedParallelismNumber) {
        this.expectedParallelismNumber = expectedParallelismNumber;
    }

    public int getDryRun() {
        return dryRun;
    }

    public void setDryRun(int dryRun) {
        this.dryRun = dryRun;
    }

    public ComplementDependentMode getComplementDependentMode() {
        return complementDependentMode;
    }

    public void setComplementDependentMode(ComplementDependentMode complementDependentMode) {
        this.complementDependentMode = complementDependentMode;
    }
}
