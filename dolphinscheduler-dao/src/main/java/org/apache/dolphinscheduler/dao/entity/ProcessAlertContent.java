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
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class ProcessAlertContent implements Serializable {

    @JsonProperty("projectId")
    private Integer projectId;
    @JsonProperty("projectCode")
    private Long projectCode;
    @JsonProperty("projectName")
    private String projectName;
    @JsonProperty("owner")
    private String owner;
    @JsonProperty("processId")
    private Integer processId;
    @JsonProperty("processDefinitionCode")
    private Long processDefinitionCode;
    @JsonProperty("processName")
    private String processName;
    @JsonProperty("processType")
    private CommandType processType;
    @JsonProperty("processState")
    private WorkflowExecutionStatus processState;
    @JsonProperty("recovery")
    private Flag recovery;
    @JsonProperty("runTimes")
    private Integer runTimes;
    @JsonProperty("processStartTime")
    private Date processStartTime;
    @JsonProperty("processEndTime")
    private Date processEndTime;
    @JsonProperty("processHost")
    private String processHost;
    @JsonProperty("taskCode")
    private Long taskCode;
    @JsonProperty("taskName")
    private String taskName;
    @JsonProperty("event")
    private AlertEvent event;
    @JsonProperty("warnLevel")
    private AlertWarnLevel warnLevel;
    @JsonProperty("taskType")
    private String taskType;
    @JsonProperty("retryTimes")
    private Integer retryTimes;
    @JsonProperty("taskState")
    private TaskExecutionStatus taskState;
    @JsonProperty("taskStartTime")
    private Date taskStartTime;
    @JsonProperty("taskEndTime")
    private Date taskEndTime;
    @JsonProperty("taskHost")
    private String taskHost;
    @JsonProperty("logPath")
    private String logPath;

}
