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

import org.apache.dolphinscheduler.common.enums.AlertEvent;
import org.apache.dolphinscheduler.common.enums.AlertWarnLevel;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.Flag;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ProcessAlertContent implements Serializable {
    @JsonProperty("projectId")
    private int projectId;
    @JsonProperty("projectName")
    private String projectName;
    @JsonProperty("owner")
    private String owner;
    @JsonProperty("processId")
    private int processId;
    @JsonProperty("processName")
    private String processName;
    @JsonProperty("processType")
    private CommandType processType;
    @JsonProperty("processState")
    private ExecutionStatus processState;
    @JsonProperty("recovery")
    private Flag recovery;
    @JsonProperty("runTimes")
    private int runTimes;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("processStartTime")
    private Date processStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("processEndTime")
    private Date processEndTime;
    @JsonProperty("processHost")
    private String processHost;
    @JsonProperty("taskId")
    private int taskId;
    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("event")
    private AlertEvent event;
    @JsonProperty("warnLevel")
    private AlertWarnLevel warnLevel;
    @JsonProperty("taskType")
    private String taskType;
    @JsonProperty("retryTimes")
    private int retryTimes;
    @JsonProperty("taskState")
    private ExecutionStatus taskState;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("taskStartTime")
    private Date taskStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("taskEndTime")
    private Date taskEndTime;
    @JsonProperty("taskHost")
    private String taskHost;
    @JsonProperty("logPath")
    private String logPath;

    private ProcessAlertContent(Builder builder) {
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.owner = builder.owner;
        this.processId = builder.processId;
        this.processName = builder.processName;
        this.processType = builder.processType;
        this.recovery = builder.recovery;
        this.processState = builder.processState;
        this.runTimes = builder.runTimes;
        this.processStartTime = builder.processStartTime;
        this.processEndTime = builder.processEndTime;
        this.processHost = builder.processHost;
        this.taskId = builder.taskId;
        this.taskName = builder.taskName;
        this.event = builder.event;
        this.warnLevel = builder.warnLevel;
        this.taskType = builder.taskType;
        this.taskState = builder.taskState;
        this.taskStartTime = builder.taskStartTime;
        this.taskEndTime = builder.taskEndTime;
        this.taskHost = builder.taskHost;
        this.logPath = builder.logPath;
        this.retryTimes = builder.retryTimes;

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int projectId;
        private String projectName;
        private String owner;
        private int processId;
        private String processName;
        private CommandType processType;
        private Flag recovery;
        private ExecutionStatus processState;
        private int runTimes;
        private Date processStartTime;
        private Date processEndTime;
        private String processHost;
        private int taskId;
        private String taskName;
        private AlertEvent event;
        private AlertWarnLevel warnLevel;
        private String taskType;
        private int retryTimes;
        private ExecutionStatus taskState;
        private Date taskStartTime;
        private Date taskEndTime;
        private String taskHost;
        private String logPath;

        public Builder projectId(int projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder processId(int processId) {
            this.processId = processId;
            return this;
        }

        public Builder processName(String processName) {
            this.processName = processName;
            return this;
        }

        public Builder processType(CommandType processType) {
            this.processType = processType;
            return this;
        }

        public Builder recovery(Flag recovery) {
            this.recovery = recovery;
            return this;
        }

        public Builder processState(ExecutionStatus processState) {
            this.processState = processState;
            return this;
        }

        public Builder runTimes(int runTimes) {
            this.runTimes = runTimes;
            return this;
        }

        public Builder processStartTime(Date processStartTime) {
            this.processStartTime = processStartTime;
            return this;
        }

        public Builder processEndTime(Date processEndTime) {
            this.processEndTime = processEndTime;
            return this;
        }

        public Builder processHost(String processHost) {
            this.processHost = processHost;
            return this;
        }

        public Builder taskId(int taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder event(AlertEvent event) {
            this.event = event;
            return this;
        }

        public Builder warningLevel(AlertWarnLevel warnLevel) {
            this.warnLevel = warnLevel;
            return this;
        }

        public Builder taskType(String taskType) {
            this.taskType = taskType;
            return this;
        }

        public Builder retryTimes(int retryTimes) {
            this.retryTimes = retryTimes;
            return this;
        }

        public Builder taskState(ExecutionStatus taskState) {
            this.taskState = taskState;
            return this;
        }

        public Builder taskStartTime(Date taskStartTime) {
            this.taskStartTime = taskStartTime;
            return this;
        }

        public Builder taskEndTime(Date taskEndTime) {
            this.taskEndTime = taskEndTime;
            return this;
        }

        public Builder taskHost(String taskHost) {
            this.taskHost = taskHost;
            return this;
        }

        public Builder logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public ProcessAlertContent build() {
            return new ProcessAlertContent(this);
        }
    }
}
