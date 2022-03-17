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

import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class TaskAlertContent implements Serializable {
    @JsonProperty("taskInstanceId")
    private int taskInstanceId;
    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("taskType")
    private String taskType;
    @JsonProperty("processDefinitionId")
    private int processDefinitionId;
    @JsonProperty("processDefinitionName")
    private String processDefinitionName;
    @JsonProperty("processInstanceId")
    private int processInstanceId;
    @JsonProperty("processInstanceName")
    private String processInstanceName;
    @JsonProperty("state")
    private ExecutionStatus state;
    @JsonProperty("startTime")
    private Date startTime;
    @JsonProperty("endTime")
    private Date endTime;
    @JsonProperty("host")
    private String host;
    @JsonProperty("logPath")
    private String logPath;

    private TaskAlertContent(Builder builder) {
        this.taskInstanceId = builder.taskInstanceId;
        this.taskName = builder.taskName;
        this.taskType = builder.taskType;
        this.processDefinitionId = builder.processDefinitionId;
        this.processDefinitionName = builder.processDefinitionName;
        this.processInstanceId = builder.processInstanceId;
        this.processInstanceName = builder.processInstanceName;
        this.state = builder.state;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
        this.host = builder.host;
        this.logPath = builder.logPath;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int taskInstanceId;
        private String taskName;
        private String taskType;
        private int processDefinitionId;
        private String processDefinitionName;
        private int processInstanceId;
        private String processInstanceName;
        private ExecutionStatus state;
        private Date startTime;
        private Date endTime;
        private String host;
        private String logPath;

        public Builder taskInstanceId(int taskInstanceId) {
            this.taskInstanceId = taskInstanceId;
            return this;
        }

        public Builder taskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        public Builder taskType(String taskType) {
            this.taskType = taskType;
            return this;
        }

        public Builder processDefinitionId(int processDefinitionId) {
            this.processDefinitionId = processDefinitionId;
            return this;
        }

        public Builder processDefinitionName(String processDefinitionName) {
            this.processDefinitionName = processDefinitionName;
            return this;
        }

        public Builder processInstanceId(int processInstanceId) {
            this.processInstanceId = processInstanceId;
            return this;
        }

        public Builder processInstanceName(String processInstanceName) {
            this.processInstanceName = processInstanceName;
            return this;
        }

        public Builder state(ExecutionStatus state) {
            this.state = state;
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Date endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder logPath(String logPath) {
            this.logPath = logPath;
            return this;
        }

        public TaskAlertContent build() {
            return new TaskAlertContent(this);
        }
    }
}
